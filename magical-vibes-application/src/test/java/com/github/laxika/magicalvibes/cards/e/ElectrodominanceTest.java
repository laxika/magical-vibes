package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.b.BankruptInBlood;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.q.Quench;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Electrodominance.class, Forest.class, GrizzlyBears.class, HillGiant.class,
        BankruptInBlood.class, EssenceCapture.class, Quench.class})
class ElectrodominanceTest extends BaseCardTest {

    @Test
    @DisplayName("Deals X damage and offers a nonland hand spell with mana value X or less")
    void dealsDamageAndOffersEligibleHandSpell() {
        Electrodominance electrodominance = new Electrodominance();
        GrizzlyBears eligibleSpell = new GrizzlyBears();
        HillGiant tooExpensiveSpell = new HillGiant();
        Forest land = new Forest();
        harness.setHand(player1, new ArrayList<>(List.of(electrodominance, eligibleSpell, tooExpensiveSpell, land)));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, 2, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(eligibleSpell.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Does not offer hand spells whose mana value exceeds X")
    void noOfferForSpellsAboveX() {
        Electrodominance electrodominance = new Electrodominance();
        HillGiant tooExpensiveSpell = new HillGiant();
        harness.setHand(player1, new ArrayList<>(List.of(electrodominance, tooExpensiveSpell)));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, 1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(tooExpensiveSpell);
    }

    @Test
    @DisplayName("Declining the free cast leaves the eligible spell in hand")
    void decliningFreeCastLeavesSpellInHand() {
        Electrodominance electrodominance = new Electrodominance();
        GrizzlyBears eligibleSpell = new GrizzlyBears();
        harness.setHand(player1, new ArrayList<>(List.of(electrodominance, eligibleSpell)));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, 2, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).contains(eligibleSpell);
    }

    @Test
    void decliningDoesNotRevealTheUncastCard() {
        harness.setHand(player1, List.of(new Electrodominance(), new BankruptInBlood()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castInstant(player1, 0, 2, player2.getId());
        harness.passBothPriorities();
        int logSize = gd.gameLog.size();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.gameLog.subList(logSize, gd.gameLog.size()).stream()
                .map(GameLogEntry::plainText)).noneMatch(log -> log.contains("Bankrupt in Blood"));
        harness.assertInHand(player1, "Bankrupt in Blood");
    }

    @Test
    void cannotCastSpellWithoutMandatoryAdditionalCost() {
        BankruptInBlood spell = new BankruptInBlood();
        harness.setHand(player1, List.of(new Electrodominance(), spell));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castInstant(player1, 0, 2, player2.getId());
        harness.passBothPriorities();

        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).contains(spell);
    }

    @Test
    void spellWithNoLegalTargetsStaysInHand() {
        EssenceCapture spell = new EssenceCapture();
        harness.setHand(player1, List.of(new Electrodominance(), spell));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castInstant(player1, 0, 2, player2.getId());
        harness.passBothPriorities();

        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.playerHands.get(player1.getId())).contains(spell);
        harness.assertNotInGraveyard(player1, "Essence Capture");
    }

    @Test
    void freeSpellCanTargetResolvingElectrodominance() {
        Electrodominance electrodominance = new Electrodominance();
        Quench spell = new Quench();
        harness.setHand(player1, List.of(electrodominance, spell));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castInstant(player1, 0, 2, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).contains(electrodominance.getId());
        harness.handlePermanentChosen(player1, electrodominance.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(spell.getId());
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Quench");
    }

    @Test
    void zeroXDealsNoDamageAndDoesNotOfferPositiveManaValueSpell() {
        Electrodominance ineligibleSpell = new Electrodominance();
        harness.setHand(player1, List.of(new Electrodominance(), ineligibleSpell));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(ineligibleSpell);
    }

    @Test
    void acceptingOneSpellRemovesOtherFreeCastOffers() {
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        harness.setHand(player1, List.of(new Electrodominance(), first, second));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castInstant(player1, 0, 2, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(first.getId());
        assertThat(gd.playerHands.get(player1.getId())).contains(second);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    void lethallyDamagedCreatureRemainsUntilTheFreeCastChoiceFinishes() {
        GrizzlyBears target = new GrizzlyBears();
        harness.addToBattlefield(player2, target);
        harness.setHand(player1, List.of(new Electrodominance(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castInstant(player1, 0, 2, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.handleMayAbilityChosen(player1, true);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.stack).hasSize(1);
    }
}
