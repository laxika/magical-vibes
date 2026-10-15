package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DistortingLens;
import com.github.laxika.magicalvibes.cards.r.RoyalAssassin;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.Terror;
import com.github.laxika.magicalvibes.model.GameData;
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

@CardUsed({KnightOfGrace.class, GrizzlyBears.class, RoyalAssassin.class, Shock.class, Terror.class, DistortingLens.class})
class KnightOfGraceTest extends BaseCardTest {

    

    @Test
    @DisplayName("Black spells from opponent cannot target Knight of Grace")
    void blackSpellsFromOpponentCannotTarget() {
        harness.addToBattlefield(player1, new KnightOfGrace());

        // Add valid target so spell is playable
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.setHand(player2, List.of(new Terror()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, harness.getPermanentId(player1, "Knight of Grace")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof from black");
    }

    @Test
    @DisplayName("Non-black spells can target Knight of Grace")
    void nonBlackSpellsCanTarget() {
        harness.addToBattlefield(player2, new KnightOfGrace());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Knight of Grace"));

        GameData gd = harness.getGameData();
        assertThat(gd.stack)
                .anyMatch(se -> se.getCard().getName().equals("Shock"));
    }

    @Test
    @DisplayName("Controller's own black spells can target Knight of Grace")
    void ownBlackSpellsCanTarget() {
        harness.addToBattlefield(player1, new KnightOfGrace());

        harness.setHand(player1, List.of(new Terror()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        // Controller targets their own Knight — hexproof from black doesn't block own spells
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Knight of Grace"));

        GameData gd = harness.getGameData();
        assertThat(gd.stack)
                .anyMatch(se -> se.getCard().getName().equals("Terror"));
    }

    @Test
    @DisplayName("Black activated abilities from opponent cannot target Knight of Grace")
    void blackActivatedAbilitiesFromOpponentCannotTarget() {
        Permanent knight = addCreatureReady(player1, new KnightOfGrace());
        knight.tap(); // Royal Assassin requires tapped target

        // Add valid target so ability is usable
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.tap();

        addCreatureReady(player2, new RoyalAssassin());

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, knight.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Gets +1/+0 when any player controls a black permanent")
    void boostWhenAnyPlayerControlsBlackPermanent() {
        harness.addToBattlefield(player1, new KnightOfGrace());

        Permanent knightPerm = findPermanent(player1, "Knight of Grace");

        // No black permanent — no bonus
        var bonus = gqs.computeStaticBonus(gd, knightPerm);
        assertThat(bonus.power()).isEqualTo(0);

        // Add a black permanent to opponent's battlefield
        harness.addToBattlefield(player2, new RoyalAssassin());

        bonus = gqs.computeStaticBonus(gd, knightPerm);
        assertThat(bonus.power()).isEqualTo(1);
        assertThat(bonus.toughness()).isEqualTo(0);
    }

    @Test
    @DisplayName("Gets +1/+0 when controller controls a black permanent")
    void boostWhenControllerControlsBlackPermanent() {
        harness.addToBattlefield(player1, new KnightOfGrace());

        Permanent knightPerm = findPermanent(player1, "Knight of Grace");

        // No black permanent — no bonus
        var bonus = gqs.computeStaticBonus(gd, knightPerm);
        assertThat(bonus.power()).isEqualTo(0);

        // Add a black permanent to controller's own battlefield
        harness.addToBattlefield(player1, new RoyalAssassin());

        bonus = gqs.computeStaticBonus(gd, knightPerm);
        assertThat(bonus.power()).isEqualTo(1);
        assertThat(bonus.toughness()).isEqualTo(0);
    }

    @Test
    @DisplayName("No boost when no black permanents exist")
    void noBoostWithoutBlackPermanents() {
        harness.addToBattlefield(player1, new KnightOfGrace());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent knightPerm = findPermanent(player1, "Knight of Grace");

        var bonus = gqs.computeStaticBonus(gd, knightPerm);
        assertThat(bonus.power()).isEqualTo(0);
        assertThat(bonus.toughness()).isEqualTo(0);
    }

    @Test
    void firstStrikeKillsBlockerBeforeRegularDamage() {
        addCreatureReady(player1, new KnightOfGrace());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Knight of Grace");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(findPermanent(player1, "Knight of Grace").getMarkedDamage()).isZero();
    }

    @Test
    void bonusDoesNotStackAndDisappearsWhenLastBlackPermanentLeaves() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new KnightOfGrace());
        Permanent ownAssassin = harness.addToBattlefieldAndReturn(player1, new RoyalAssassin());
        Permanent opposingAssassin = harness.addToBattlefieldAndReturn(player2, new RoyalAssassin());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(3);
        harness.castInstant(player1, 0, ownAssassin.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(3);

        harness.castInstant(player1, 0, opposingAssassin.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(2);
    }

    @Test
    void ownBlackAbilityCanDestroyKnight() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new KnightOfGrace());
        knight.tap();
        addCreatureReady(player1, new RoyalAssassin());

        harness.activateAbility(player1, 1, null, knight.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Knight of Grace");
        harness.assertInGraveyard(player1, "Knight of Grace");
    }

    @Test
    void opponentBlackAbilityCanTargetAfterItsSourceBecomesRed() {
        Permanent knight = harness.addToBattlefieldAndReturn(player2, new KnightOfGrace());
        knight.tap();
        Permanent assassin = addCreatureReady(player1, new RoyalAssassin());
        harness.addToBattlefield(player1, new DistortingLens());

        harness.activateAbility(player1, 1, null, assassin.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(2);

        harness.activateAbility(player1, 0, null, knight.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Knight of Grace");
    }

    @Test
    void abilityBecomesIllegalWhenSourceBecomesBlackBeforeResolution() {
        Permanent knight = harness.addToBattlefieldAndReturn(player2, new KnightOfGrace());
        harness.addToBattlefield(player1, new DistortingLens());
        harness.addToBattlefield(player2, new DistortingLens());

        harness.activateAbility(player1, 0, null, knight.getId());
        harness.activateAbility(player2, 1, null, harness.getPermanentId(player1, "Distorting Lens"));
        harness.passBothPriorities();
        harness.handleListChoice(player2, "BLACK");
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(3);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player2, "Knight of Grace");
    }
}
