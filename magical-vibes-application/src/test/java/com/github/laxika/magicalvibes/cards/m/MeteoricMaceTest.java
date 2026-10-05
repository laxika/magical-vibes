package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Harrow;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MeteoricMace.class, GrizzlyBears.class, Forest.class, LlanowarElves.class, Harrow.class})
class MeteoricMaceTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +4/+0 and trample")
    void equippedCreatureGetsBonusAndTrample() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent mace = addMaceReady(player1);
        mace.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Equip {4} attaches Meteoric Mace to a creature you control")
    void equipAttachesMace() {
        Permanent mace = addMaceReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(mace.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Cascade skips lands and casts the first cheaper nonland card for free")
    void cascadeCastsFirstCheaperNonland() {
        LlanowarElves hit = new LlanowarElves();
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(land, hit));
        castMace();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class))
                .isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards().stream().map(Card::getName).toList())
                .containsExactly("Llanowar Elves");

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Llanowar Elves"));
    }

    @Test
    void reequippingMovesBonusAndTrampleToNewCreature() {
        Permanent mace = addMaceReady(player1);
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        mace.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(mace.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, first, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, second, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void decliningCascadeReturnsHitAndSkippedCardsBelowUntouchedLibrary() {
        Forest skipped = new Forest();
        GrizzlyBears hit = new GrizzlyBears();
        LlanowarElves untouched = new LlanowarElves();
        harness.setLibrary(player1, List.of(skipped, hit, untouched));
        castMace();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 3))
                .containsExactlyInAnyOrder(skipped, hit);
        assertThat(gd.findExiledCard(skipped.getId())).isNull();
        assertThat(gd.findExiledCard(hit.getId())).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Meteoric Mace");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void cascadeDoesNotCastEqualManaValueCard() {
        Forest land = new Forest();
        MeteoricMace equalCost = new MeteoricMace();
        harness.setLibrary(player1, List.of(land, equalCost));
        castMace();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(land, equalCost);
        assertThat(gd.findExiledCard(land.getId())).isNull();
        assertThat(gd.findExiledCard(equalCost.getId())).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Meteoric Mace");
    }

    @Test
    void emptyLibraryDoesNotPreventMaceFromResolving() {
        harness.setLibrary(player1, List.of());
        castMace();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Meteoric Mace");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({MeteoricMace.class, Harrow.class, Forest.class})
    void cascadeRequiresHarrowsAdditionalLandSacrificeBeforeCasting() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Harrow hit = new Harrow();
        harness.setLibrary(player1, List.of(hit));
        castMace();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        if (gd.interaction.activeInteraction() instanceof PendingInteraction.PermanentChoice) {
            harness.handlePermanentChosen(player1, land.getId());
        }

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(land);
        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(hit.getId()));
    }

    private void castMace() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new MeteoricMace(), "{4}{R}{R}");
    }

    private Permanent addMaceReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new MeteoricMace());
        permanent.setSummoningSick(false);
        return permanent;
    }

}
