package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.g.GoblinInstigator;
import com.github.laxika.magicalvibes.cards.i.Immersturm;
import com.github.laxika.magicalvibes.cards.v.ViashinoPyromancer;
import com.github.laxika.magicalvibes.cards.z.ZuranSpellcaster;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AmuletOfSafekeeping.class, Shock.class, GoblinInstigator.class, ViashinoPyromancer.class})
class AmuletOfSafekeepingTest extends BaseCardTest {

    @Test
    @DisplayName("Creature tokens get -1/-0, but nontoken creatures do not")
    void debuffsCreatureTokens() {
        harness.addToBattlefield(player1, new AmuletOfSafekeeping());
        harness.addToBattlefield(player1, createCreature("Soldier Token", 2, 2, true));
        harness.addToBattlefield(player2, createCreature("Zombie Token", 3, 3, true));
        harness.addToBattlefield(player1, createCreature("Grizzly Bears", 2, 2, false));

        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "Soldier Token"))).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, findPermanent(player1, "Soldier Token"))).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, findPermanent(player2, "Zombie Token"))).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "Grizzly Bears"))).isEqualTo(2);
    }

    @Test
    @DisplayName("Counters an opponent's spell that targets its controller")
    void countersOpponentSpellTargetingController() {
        harness.addToBattlefield(player1, new AmuletOfSafekeeping());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not counter its controller's own spell")
    void doesNotCounterControllersOwnSpell() {
        harness.addToBattlefield(player1, new AmuletOfSafekeeping());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("Counters an opponent's ability that targets its controller")
    @CardUsed({ZuranSpellcaster.class})
    void countersOpponentAbilityTargetingController() {
        harness.addToBattlefield(player1, new AmuletOfSafekeeping());
        Permanent spellcaster = addCreatureReady(player2, new ZuranSpellcaster());

        harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(spellcaster), null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(spellcaster.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The opponent may pay {1} to keep the spell")
    void opponentMayPay() {
        harness.addToBattlefield(player1, new AmuletOfSafekeeping());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    void opponentMayDeclinePaymentWithManaAvailable() {
        harness.addToBattlefield(player1, new AmuletOfSafekeeping());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotProtectControllersCreatures() {
        harness.addToBattlefield(player1, new AmuletOfSafekeeping());
        harness.addToBattlefield(player1, new GoblinInstigator());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Goblin Instigator"));

        harness.assertInGraveyard(player1, "Goblin Instigator");
        harness.assertInGraveyard(player2, "Shock");
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotProtectOpponent() {
        harness.addToBattlefield(player1, new AmuletOfSafekeeping());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player2.getId());

        harness.assertLife(player2, 18);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void eachAmuletRequiresItsOwnPayment() {
        harness.addToBattlefield(player1, new AmuletOfSafekeeping());
        harness.addToBattlefield(player1, new AmuletOfSafekeeping());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void countersOpponentTriggeredAbility() {
        harness.addToBattlefield(player1, new AmuletOfSafekeeping());
        harness.setHand(player2, List.of(new ViashinoPyromancer()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castCreature(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player2, "Viashino Pyromancer");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({ZuranSpellcaster.class})
    void opponentMayPayToKeepActivatedAbility() {
        harness.addToBattlefield(player1, new AmuletOfSafekeeping());
        Permanent spellcaster = addCreatureReady(player2, new ZuranSpellcaster());
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(spellcaster), null, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        assertThat(spellcaster.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void multipleAmuletsReduceNewTokensBelowZeroPowerWithoutReducingToughness() {
        harness.addToBattlefield(player1, new AmuletOfSafekeeping());
        harness.addToBattlefield(player1, new AmuletOfSafekeeping());
        harness.setHand(player2, List.of(new GoblinInstigator()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent token = findPermanent(player2, "Goblin");
        Permanent instigator = findPermanent(player2, "Goblin Instigator");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, instigator)).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Goblin");
    }

    @Test
    @CardUsed({Immersturm.class, ZuranSpellcaster.class})
    void countersOpponentPlanarAbilityTargetingController() {
        harness.addToBattlefield(player1, new AmuletOfSafekeeping());
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player2.getId();
        gd.planechase.faceUp.add(new PlanarObject(new Immersturm(), gd.nextTimestamp()));

        harness.enterBattlefieldAndReturn(player2, new ZuranSpellcaster());
        harness.inMutationScope(() -> harness.getTriggerCollectionService().processNextSpellTargetTrigger(gd));
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player2, "Zuran Spellcaster");
    }

    private Card createCreature(String name, int power, int toughness, boolean token) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{2}");
        card.setColor(CardColor.GREEN);
        card.setPower(power);
        card.setToughness(toughness);
        card.setToken(token);
        return card;
    }
}
