package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.c.CradleClearcutter;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MightstonesAnimation;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TomakulHonorGuard;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OverTheTop.class, Forest.class, GrizzlyBears.class, RodOfRuin.class, Shock.class,
        CradleClearcutter.class, MightstonesAnimation.class, TomakulHonorGuard.class})
class OverTheTopTest extends BaseCardTest {

    @Test
    @DisplayName("Each player reveals based on their nonland permanent count")
    void eachPlayerRevealsBasedOnTheirNonlandPermanentCount() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        RodOfRuin player1Permanent = new RodOfRuin();
        Shock player1Spell = new Shock();
        GrizzlyBears player2Permanent = new GrizzlyBears();
        Shock player2Spell = new Shock();
        harness.setLibrary(player1, List.of(player1Permanent, player1Spell));
        harness.setLibrary(player2, List.of(player2Permanent, player2Spell));

        harness.setHand(player1, List.of(new OverTheTop()));
        harness.addMana(player1, ManaColor.RED, 7);
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertOnBattlefield(player1, "Rod of Ruin");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(player1Spell);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(player2Spell);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Lands are not counted when determining how many cards to reveal")
    void landsAreNotCounted() {
        harness.addToBattlefield(player1, new Forest());
        Shock shock = new Shock();
        RodOfRuin rodOfRuin = new RodOfRuin();
        harness.setLibrary(player1, List.of(shock, rodOfRuin));
        harness.setLibrary(player2, List.of());

        harness.setHand(player1, List.of(new OverTheTop()));
        harness.addMana(player1, ManaColor.RED, 7);
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(shock, rodOfRuin);
    }

    @Test
    @DisplayName("Revealed lands enter even when fewer cards remain than the reveal count")
    void revealedLandEntersFromShortLibrary() {
        harness.addToBattlefield(player1, new CradleClearcutter());
        harness.addToBattlefield(player1, new CradleClearcutter());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new OverTheTop()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(forest);
    }

    @Test
    @DisplayName("A revealed Aura with no legal attachment remains on top of the library")
    void auraWithoutLegalAttachmentRemainsInLibrary() {
        harness.addToBattlefield(player1, new TomakulHonorGuard());
        MightstonesAnimation aura = new MightstonesAnimation();
        Forest nextCard = new Forest();
        harness.setLibrary(player1, List.of(aura, nextCard));
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new OverTheTop()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(aura, nextCard);
        harness.assertNotOnBattlefield(player1, "Mightstone's Animation");
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(aura);
    }

    @Test
    @DisplayName("A revealed Aura can enchant an artifact already on the battlefield")
    void revealedAuraAttachesToExistingArtifact() {
        var artifact = harness.addToBattlefieldAndReturn(player1, new CradleClearcutter());
        MightstonesAnimation aura = new MightstonesAnimation();
        harness.setLibrary(player1, List.of(aura, new Forest()));
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new OverTheTop()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castAndResolveSorcery(player1, 0, 0);
        if (gd.interaction.isAwaitingInput()) {
            harness.handlePermanentChosen(player1, artifact.getId());
        }

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> {
                    assertThat(permanent.getCard()).isSameAs(aura);
                    assertThat(permanent.getAttachedTo()).isEqualTo(artifact.getId());
                });
    }

    @Test
    @DisplayName("Players reveal different amounts and newly entered permanents do not increase them")
    void eachPlayersRevealCountIsFixedBeforePermanentsEnter() {
        harness.addToBattlefield(player1, new CradleClearcutter());
        harness.addToBattlefield(player2, new TomakulHonorGuard());
        harness.addToBattlefield(player2, new CradleClearcutter());
        Forest player1Land = new Forest();
        Forest player1Unrevealed = new Forest();
        Forest player2Land = new Forest();
        CradleClearcutter player2Creature = new CradleClearcutter();
        Forest player2Unrevealed = new Forest();
        harness.setLibrary(player1, List.of(player1Land, player1Unrevealed));
        harness.setLibrary(player2, List.of(player2Land, player2Creature, player2Unrevealed));
        harness.setHand(player1, List.of(new OverTheTop()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(player1Unrevealed);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(player2Unrevealed);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard()).contains(player1Land);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard()).contains(player2Land, player2Creature);
    }
}
