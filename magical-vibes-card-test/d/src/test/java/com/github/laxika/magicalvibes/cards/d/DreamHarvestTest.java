package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.IronShieldElf;
import com.github.laxika.magicalvibes.cards.e.EndBlazeEpiphany;
import com.github.laxika.magicalvibes.cards.r.RequitingHex;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DreamHarvest.class, Forest.class, IronShieldElf.class,
        RequitingHex.class, EndBlazeEpiphany.class})
class DreamHarvestTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles each opponent's cards until total mana value is at least five")
    void exilesUntilTotalManaValueFive() {
        Forest firstLand = new Forest();
        IronShieldElf firstSpell = new IronShieldElf();
        Forest secondLand = new Forest();
        IronShieldElf secondSpell = new IronShieldElf();
        IronShieldElf thirdSpell = new IronShieldElf();
        Forest remainingCard = new Forest();
        harness.setLibrary(player2,
                List.of(firstLand, firstSpell, secondLand, secondSpell, thirdSpell, remainingCard));

        harness.setHand(player1, List.of(new DreamHarvest()));
        harness.addMana(player1, ManaColor.BLUE, 7);
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(firstLand.getId(), firstSpell.getId(), secondLand.getId(),
                        secondSpell.getId(), thirdSpell.getId());
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remainingCard);
        assertThat(gd.exilePlayPermissions)
                .containsEntry(firstSpell.getId(), player1.getId())
                .containsEntry(secondSpell.getId(), player1.getId())
                .containsEntry(thirdSpell.getId(), player1.getId())
                .doesNotContainKey(firstLand.getId())
                .doesNotContainKey(secondLand.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost)
                .contains(firstSpell.getId(), secondSpell.getId(), thirdSpell.getId())
                .doesNotContain(firstLand.getId(), secondLand.getId());
    }

    @Test
    @DisplayName("An exiled spell can be cast without mana until end of turn")
    void castsExiledSpellWithoutMana() {
        IronShieldElf firstSpell = new IronShieldElf();
        IronShieldElf secondSpell = new IronShieldElf();
        IronShieldElf thirdSpell = new IronShieldElf();
        harness.setLibrary(player2, List.of(firstSpell, secondSpell, thirdSpell));

        harness.setHand(player1, List.of(new DreamHarvest()));
        harness.addMana(player1, ManaColor.BLUE, 7);
        harness.castAndResolveSorcery(player1, 0, 0);
        gd.playerManaPools.get(player1.getId()).clear();

        harness.castFromExile(player1, firstSpell.getId());

        assertThat(gd.stack)
                .anyMatch(entry -> entry.getCard().getId().equals(firstSpell.getId())
                        && entry.getEntryType() == StackEntryType.CREATURE_SPELL);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(firstSpell.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost).doesNotContain(firstSpell.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Iron-Shield Elf");
        harness.assertNotOnBattlefield(player2, "Iron-Shield Elf");
    }

    @Test
    void stopsAtExactlyFiveManaValueAndDoesNotExileControllersLibrary() {
        RequitingHex first = new RequitingHex();
        IronShieldElf second = new IronShieldElf();
        IronShieldElf third = new IronShieldElf();
        Forest remaining = new Forest();
        Forest ownCard = new Forest();
        harness.setLibrary(player1, List.of(ownCard));
        harness.setLibrary(player2, List.of(first, second, third, remaining));
        resolveHarvest();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(first, second, third);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remaining);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownCard);
    }

    @Test
    void exhaustsLibraryBelowThresholdAndStillAllowsCasting() {
        Forest land = new Forest();
        IronShieldElf spell = new IronShieldElf();
        harness.setLibrary(player2, List.of(land, spell));
        resolveHarvest();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(land, spell);
        gd.playerManaPools.get(player1.getId()).clear();
        harness.castFromExile(player1, spell.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Iron-Shield Elf");
    }

    @Test
    void emptyLibraryDoesNotPreventResolution() {
        harness.setLibrary(player2, List.of());
        resolveHarvest();

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Dream Harvest");
    }

    @Test
    void exiledLandsCannotBePlayed() {
        Forest land = new Forest();
        harness.setLibrary(player2, List.of(land));
        resolveHarvest();

        assertThatThrownBy(() -> harness.castFromExile(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(land);
    }

    @Test
    void creatureStillRequiresNormalCastingTiming() {
        IronShieldElf spell = new IronShieldElf();
        harness.setLibrary(player2, List.of(spell));
        resolveHarvest();
        harness.forceStep(TurnStep.END_STEP);

        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(spell);
    }

    @Test
    void permissionExpiresAfterTheTurnAndCardsRemainExiled() {
        IronShieldElf spell = new IronShieldElf();
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(spell, new IronShieldElf(), new IronShieldElf(),
                new Forest(), new Forest()));
        resolveHarvest();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(spell);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(spell.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost).doesNotContain(spell.getId());
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 2);
        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opponentCannotCastCardsExiledByHarvest() {
        IronShieldElf spell = new IronShieldElf();
        harness.setLibrary(player2, List.of(spell));
        resolveHarvest();
        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castFromExile(player2, spell.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(spell);
    }

    @Test
    void canDeclineOptionalAdditionalCostOfExiledSpell() {
        RequitingHex spell = new RequitingHex();
        harness.setLibrary(player2, List.of(spell));
        harness.addToBattlefield(player2, new IronShieldElf());
        resolveHarvest();
        gd.playerManaPools.get(player1.getId()).clear();

        harness.castFromExile(player1, spell.getId(),
                harness.getPermanentId(player2, "Iron-Shield Elf"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Iron-Shield Elf");
        harness.assertInGraveyard(player2, "Requiting Hex");
        harness.assertLife(player1, 20);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void cannotChooseNonzeroXWhenCastingWithoutPayingManaCost() {
        EndBlazeEpiphany spell = new EndBlazeEpiphany();
        harness.setLibrary(player2, List.of(spell));
        harness.addToBattlefield(player2, new IronShieldElf());
        resolveHarvest();
        gd.playerManaPools.get(player1.getId()).clear();
        var targetId = harness.getPermanentId(player2, "Iron-Shield Elf");
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> harness.inMutationScope(() ->
                harness.getSpellCastingService().playCardFromExile(
                        gd, player1, spell.getId(), 3, targetId)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(spell);
    }

    @Test
    void xIsZeroInLibraryAndCanBeCastForZero() {
        EndBlazeEpiphany spell = new EndBlazeEpiphany();
        IronShieldElf second = new IronShieldElf();
        IronShieldElf third = new IronShieldElf();
        Forest remaining = new Forest();
        harness.setLibrary(player2, List.of(spell, second, third, remaining));
        harness.addToBattlefield(player2, new IronShieldElf());
        resolveHarvest();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remaining);
        harness.castFromExile(player1, spell.getId(),
                harness.getPermanentId(player2, "Iron-Shield Elf"));
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Iron-Shield Elf");
        harness.assertInGraveyard(player2, "End-Blaze Epiphany");
    }

    private void resolveHarvest() {
        harness.setHand(player1, List.of(new DreamHarvest()));
        harness.addMana(player1, ManaColor.BLUE, 7);
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
