package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.ArgothianSprite;
import com.github.laxika.magicalvibes.cards.c.ChaliceOfTheVoid;
import com.github.laxika.magicalvibes.cards.g.GnarlrootPallbearer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GildedLotus;
import com.github.laxika.magicalvibes.cards.w.WurmcoilEngine;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BatteryBearer.class, GrizzlyBears.class, GildedLotus.class, WurmcoilEngine.class,
        ArgothianSprite.class, BoulderbranchGolem.class, ChaliceOfTheVoid.class, GnarlrootPallbearer.class})
class BatteryBearerTest extends BaseCardTest {

    @Test
    @DisplayName("Battery Bearer gives your creatures a Powerstone mana ability")
    void grantsRestrictedManaAbilityToYourCreatures() {
        harness.addToBattlefield(player1, new BatteryBearer());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getPowerstoneOnlyColorless()).isEqualTo(1);
        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Casting an artifact with mana value 6 or greater triggers a draw")
    void largeArtifactSpellTriggersDraw() {
        harness.addToBattlefield(player1, new BatteryBearer());
        harness.setHand(player1, List.of(new WurmcoilEngine()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Casting an artifact with mana value less than 6 does not trigger a draw")
    void smallArtifactSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new BatteryBearer());
        harness.setHand(player1, List.of(new GildedLotus()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castArtifact(player1, 0);

        GameData gameData = harness.getGameData();
        assertThat(gameData.stack).hasSize(1);
        assertThat(gameData.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
    }

    @Test
    void bearerGrantsManaAbilityToItselfWithoutUsingTheStack() {
        Permanent bearer = addCreatureReady(player1, new BatteryBearer());

        harness.activateAbility(player1, 0, null, null);

        assertThat(bearer.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getPowerstoneOnlyColorless()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void grantedTapAbilityCannotBeUsedWhileSummoningSick() {
        Permanent bearer = harness.addToBattlefieldAndReturn(player1, new BatteryBearer());
        bearer.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(bearer.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getPowerstoneOnlyColorless()).isZero();
    }

    @Test
    void doesNotGrantManaAbilityToOpponentsCreatures() {
        harness.addToBattlefield(player1, new BatteryBearer());
        Permanent sprite = addCreatureReady(player2, new ArgothianSprite());

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid ability index");

        assertThat(sprite.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player2.getId()).getPowerstoneOnlyColorless()).isZero();
    }

    @Test
    void restrictedManaCannotPayForANonartifactSpell() {
        addCreatureReady(player1, new BatteryBearer());
        harness.setHand(player1, List.of(new ArgothianSprite()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getPowerstoneOnlyColorless()).isEqualTo(1);
    }

    @Test
    void restrictedManaCanPayForAnArtifactSpell() {
        addCreatureReady(player1, new BatteryBearer());
        harness.setHand(player1, List.of(new BoulderbranchGolem()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, 0, null, null);

        harness.castCreature(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getPowerstoneOnlyColorless()).isZero();
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    void restrictedManaCanPayForANonartifactCreaturesActivatedAbility() {
        addCreatureReady(player1, new BatteryBearer());
        Permanent sprite = harness.addToBattlefieldAndReturn(player1, new ArgothianSprite());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, 0, null, null);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(sprite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getPowerstoneOnlyColorless()).isZero();
    }

    @Test
    void opponentCastingALargeArtifactDoesNotTriggerDraw() {
        harness.addToBattlefield(player1, new BatteryBearer());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new BoulderbranchGolem()));
        harness.addMana(player2, ManaColor.COLORLESS, 7);

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    void largeNonartifactSpellDoesNotTriggerDraw() {
        harness.addToBattlefield(player1, new BatteryBearer());
        harness.setHand(player1, List.of(new GnarlrootPallbearer()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    void prototypeSpellUsesItsPrototypeManaValueForTheDrawCondition() {
        harness.addToBattlefield(player1, new BatteryBearer());
        harness.setHand(player1, List.of(new BoulderbranchGolem()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);

        gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null, List.of());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    void xArtifactSpellWithManaValueSixTriggersDrawBeforeResolving() {
        harness.addToBattlefield(player1, new BatteryBearer());
        harness.setHand(player1, List.of(new ChaliceOfTheVoid()));
        harness.setLibrary(player1, List.of(new BatteryBearer()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castArtifact(player1, 0, 3);

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Battery Bearer");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
        harness.assertNotOnBattlefield(player1, "Chalice of the Void");
    }

    @Test
    void xArtifactSpellBelowManaValueSixDoesNotTriggerDraw() {
        harness.addToBattlefield(player1, new BatteryBearer());
        harness.setHand(player1, List.of(new ChaliceOfTheVoid()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0, 2);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
    }
}
