package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.DuneBeetle;
import com.github.laxika.magicalvibes.cards.e.Electrify;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OketraTheTrue.class, DuneBeetle.class, Electrify.class})
class OketraTheTrueTest extends BaseCardTest {

    @Test
    @DisplayName("Can attack when controlling three other creatures")
    void canAttackWithThreeOtherCreatures() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new OketraTheTrue());
        addCreatureReady(player1, new DuneBeetle());
        addCreatureReady(player1, new DuneBeetle());
        addCreatureReady(player1, new DuneBeetle());

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isLessThan(20);
    }

    @Test
    @DisplayName("Cannot attack when controlling only two other creatures (source not counted)")
    void cannotAttackWithTwoOtherCreatures() {
        addCreatureReady(player1, new OketraTheTrue());
        addCreatureReady(player1, new DuneBeetle());
        addCreatureReady(player1, new DuneBeetle());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can block when controlling three other creatures")
    void canBlockWithThreeOtherCreatures() {
        addCreatureReady(player2, new DuneBeetle());
        addCreatureReady(player1, new OketraTheTrue());
        addCreatureReady(player1, new DuneBeetle());
        addCreatureReady(player1, new DuneBeetle());
        addCreatureReady(player1, new DuneBeetle());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        assertThat(findPermanent(player1, "Oketra the True").isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Cannot block when controlling only two other creatures")
    void cannotBlockWithTwoOtherCreatures() {
        addCreatureReady(player2, new DuneBeetle());
        addCreatureReady(player1, new OketraTheTrue());
        addCreatureReady(player1, new DuneBeetle());
        addCreatureReady(player1, new DuneBeetle());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Activated ability creates a 1/1 white Warrior token with vigilance")
    void abilityCreatesWarriorTokenWithVigilance() {
        addCreatureReady(player1, new OketraTheTrue());
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Warrior");
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.WARRIOR);
        assertThat(gqs.hasKeyword(gd, token, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Opposing creatures do not satisfy the attack restriction")
    void opposingCreaturesDoNotCount() {
        addCreatureReady(player1, new OketraTheTrue());
        addCreatureReady(player1, new DuneBeetle());
        addCreatureReady(player1, new DuneBeetle());
        addCreatureReady(player2, new DuneBeetle());
        addCreatureReady(player2, new DuneBeetle());
        addCreatureReady(player2, new DuneBeetle());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Tapped and summoning sick creatures count toward the restriction")
    void tappedAndSummoningSickCreaturesCount() {
        addCreatureReady(player1, new OketraTheTrue());
        for (int i = 0; i < 3; i++) {
            Permanent creature = addCreatureReady(player1, new DuneBeetle());
            creature.tap();
            creature.setSummoningSick(true);
        }
        harness.setLife(player2, 20);

        declareAttackers(player1, List.of(0));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("The token ability works while Oketra is tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent oketra = addCreatureReady(player1, new OketraTheTrue());
        oketra.tap();
        oketra.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Warrior")).isEqualTo(1);
        assertThat(findPermanent(player1, "Warrior").isSummoningSick()).isTrue();
        assertThat(findPermanent(player1, "Warrior").isTapped()).isFalse();
    }

    @Test
    @DisplayName("A newly created Warrior enables Oketra to attack immediately")
    void tokenEnablesAttack() {
        addCreatureReady(player1, new OketraTheTrue());
        addCreatureReady(player1, new DuneBeetle());
        addCreatureReady(player1, new DuneBeetle());
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(player1, List.of(0));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Indestructible keeps Oketra on the battlefield with lethal damage")
    void survivesLethalDamage() {
        Permanent oketra = addCreatureReady(player1, new OketraTheTrue());
        harness.setHand(player1, List.of(new Electrify(), new Electrify()));
        harness.addMana(player1, ManaColor.RED, 8);

        harness.castInstant(player1, 0, oketra.getId());
        harness.castAndResolveInstant(player1, 0, oketra.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Oketra the True");
        assertThat(oketra.getMarkedDamage()).isEqualTo(8);
    }

    @Test
    @DisplayName("The ability requires white mana")
    void cannotActivateWithoutWhiteMana() {
        addCreatureReady(player1, new OketraTheTrue());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(countPermanents(player1, "Warrior")).isZero();
    }

    @Test
    @DisplayName("The Warrior token attacks without tapping")
    void warriorTokenHasVigilanceInCombat() {
        addCreatureReady(player1, new OketraTheTrue());
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.setLife(player2, 20);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        Permanent token = findPermanent(player1, "Warrior");
        token.setSummoningSick(false);

        declareAttackers(player1, List.of(1));
        resolveCombat();

        assertThat(token.isTapped()).isFalse();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }
}
