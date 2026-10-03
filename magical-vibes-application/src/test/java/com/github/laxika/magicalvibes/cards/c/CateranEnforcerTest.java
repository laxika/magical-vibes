package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.r.RamosianSergeant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CateranEnforcer.class, CateranKidnappers.class, CateranSlaver.class, CacklingWitch.class,
        RamosianSergeant.class, CrenellatedWall.class})
class CateranEnforcerTest extends BaseCardTest {

    @Test
    void searchesForMercenaryPermanentWithManaValueAtMostFour() {
        addCreatureReady(player1, new CateranEnforcer());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.setLibrary(player1, List.of(new CateranKidnappers(), new CacklingWitch(), new CateranSlaver()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Cateran Enforcer").isTapped()).isTrue();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .extracting(Card::getName)
                .containsExactly("Cateran Kidnappers");

        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Cateran Kidnappers");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Cackling Witch", "Cateran Slaver");
    }

    @Test
    void resolvesWithoutInteractionWhenNoMercenaryPermanentWithManaValueAtMostFourExists() {
        addCreatureReady(player1, new CateranEnforcer());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setLibrary(player1, List.of(new CacklingWitch(), new CateranSlaver()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Cackling Witch", "Cateran Slaver");
    }
    @Test
    void mayFailToFindEvenWhenAnEligibleMercenaryExists() {
        addCreatureReady(player1, new CateranEnforcer());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setLibrary(player1, List.of(new CateranKidnappers(), new CacklingWitch()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Cateran Kidnappers");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Cateran Kidnappers", "Cackling Witch");
        assertThat(gd.gameLog).anyMatch(entry -> entry.plainText().contains("chooses not to take a card")
                && entry.plainText().contains("Library is shuffled"));
    }

    @Test
    void foundMercenaryEntersUntappedAndCannotImmediatelyUseItsTapAbility() {
        addCreatureReady(player1, new CateranEnforcer());
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.setLibrary(player1, List.of(new CateranKidnappers()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        Permanent kidnappers = findPermanent(player1, "Cateran Kidnappers");
        assertThat(kidnappers.isTapped()).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertNotOnBattlefield(player2, "Cateran Kidnappers");
    }

    @Test
    void cannotActivateWithInsufficientMana() {
        Permanent enforcer = addCreatureReady(player1, new CateranEnforcer());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(enforcer.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent enforcer = addCreatureReady(player1, new CateranEnforcer());
        enforcer.setTapped(true);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent enforcer = addCreatureReady(player1, new CateranEnforcer());
        enforcer.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(enforcer.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void fearAllowsBlackAndArtifactBlockersButRejectsOtherCreatures() {
        Permanent enforcer = addCreatureReady(player1, new CateranEnforcer());
        Permanent blackBlocker = addCreatureReady(player2, new CacklingWitch());
        Permanent artifactBlocker = addCreatureReady(player2, new CrenellatedWall());
        Permanent whiteBlocker = addCreatureReady(player2, new RamosianSergeant());
        List<Permanent> defenders = gd.playerBattlefields.get(player2.getId());

        assertThat(bls.canBlockAttacker(gd, blackBlocker, enforcer, defenders)).isTrue();
        assertThat(bls.canBlockAttacker(gd, artifactBlocker, enforcer, defenders)).isTrue();
        assertThat(bls.canBlockAttacker(gd, whiteBlocker, enforcer, defenders)).isFalse();
    }
}
