package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Flatten;
import com.github.laxika.magicalvibes.cards.s.ScionOfUgin;
import com.github.laxika.magicalvibes.cards.w.WelkinTern;
import com.github.laxika.magicalvibes.cards.x.Xenograft;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DragonTempest.class, DragonEgg.class, GrizzlyBears.class, WelkinTern.class,
        DragonlordDromoka.class, ScionOfUgin.class, StormwingDragon.class, Flatten.class,
        DromokaWarrior.class, Xenograft.class})
class DragonTempestTest extends BaseCardTest {

    @Test
    @DisplayName("A flying creature entering gains haste until end of turn")
    void flyingCreatureGainsHaste() {
        harness.addToBattlefield(player1, new DragonTempest());
        harness.castFromHand(player1, new WelkinTern(), "{1}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent tern = findPermanent(player1, "Welkin Tern");
        assertThat(tern.hasKeyword(Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(tern.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("A nonflying creature does not gain haste")
    void nonflyingCreatureDoesNotGainHaste() {
        harness.addToBattlefield(player1, new DragonTempest());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A Dragon entering deals damage equal to the number of Dragons controlled")
    void dragonEntryDealsDamageBasedOnDragonCount() {
        harness.addToBattlefield(player1, new DragonTempest());
        harness.addToBattlefield(player1, new DragonEgg());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.castFromHand(player1, new DragonEgg(), "{2}{R}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(victim.getId()));
    }

    @Test
    @DisplayName("A flying Dragon gains haste and deals damage with its own lifelink")
    void flyingDragonIsTheDamageSource() {
        harness.addToBattlefield(player1, new DragonTempest());
        harness.addToBattlefield(player1, new ScionOfUgin());
        harness.addToBattlefield(player2, new ScionOfUgin());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castFromHand(player1, new DragonlordDromoka(), "{4}{G}{W}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Dragonlord Dromoka").hasKeyword(Keyword.HASTE)).isTrue();
        harness.assertLife(player2, 18);
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Dragon count is determined at resolution after another Dragon leaves")
    void dragonCountUsesTheBattlefieldAtResolution() {
        harness.addToBattlefield(player1, new DragonTempest());
        Permanent otherDragon = harness.addToBattlefieldAndReturn(player1, new StormwingDragon());
        harness.setLife(player2, 20);

        harness.castFromHand(player1, new ScionOfUgin(), "{6}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.setHand(player1, List.of(new Flatten()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castAndResolveInstant(player1, 0, otherDragon.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 19);
        assertThat(countPermanents(player1, "Stormwing Dragon")).isZero();
    }

    @Test
    @DisplayName("The entering Dragon still deals damage after leaving the battlefield")
    void departedDragonStillDealsDamage() {
        harness.addToBattlefield(player1, new DragonTempest());
        harness.addToBattlefield(player1, new StormwingDragon());
        harness.setLife(player2, 20);

        harness.castFromHand(player1, new ScionOfUgin(), "{6}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        Permanent enteringDragon = findPermanent(player1, "Scion of Ugin");
        harness.setHand(player1, List.of(new Flatten()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castAndResolveInstant(player1, 0, enteringDragon.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 19);
        assertThat(countPermanents(player1, "Scion of Ugin")).isZero();
    }

    @Test
    @DisplayName("An opponent's flying Dragon triggers neither ability")
    void opponentDragonDoesNotTrigger() {
        harness.addToBattlefield(player1, new DragonTempest());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new ScionOfUgin(), "{6}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findPermanent(player2, "Scion of Ugin").hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("A creature entering as a Dragon because of Xenograft triggers damage")
    void battlefieldSubtypeGrantTriggersDragonAbility() {
        harness.addToBattlefield(player1, new DragonTempest());
        Permanent xenograft = harness.addToBattlefieldAndReturn(player1, new Xenograft());
        xenograft.setChosenSubtype(CardSubtype.DRAGON);
        harness.setLife(player2, 20);

        harness.castFromHand(player1, new DromokaWarrior(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 19);
        assertThat(findPermanent(player1, "Dromoka Warrior").hasKeyword(Keyword.HASTE)).isFalse();
    }
}
