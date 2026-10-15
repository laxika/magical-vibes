package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AncientSpider;
import com.github.laxika.magicalvibes.cards.f.ForsakenCity;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OrimsChant.class, AncientSpider.class, ForsakenCity.class})
class OrimsChantTest extends BaseCardTest {

    @Test
    @DisplayName("The target player can't cast spells for the rest of the turn")
    void targetPlayerCantCastSpells() {
        castChant(false);

        harness.setHand(player2, List.of(new OrimsChant()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Only the targeted player can't cast spells")
    void onlyTargetedPlayerCantCastSpells() {
        castChant(false);

        harness.setHand(player1, List.of(new OrimsChant()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertInGraveyard(player1, "Orim's Chant");
    }

    @Test
    @DisplayName("The targeted player can still play lands")
    void targetedPlayerCanStillPlayLands() {
        castChant(false);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new ForsakenCity()));

        harness.playLand(player2, 0);

        harness.assertOnBattlefield(player2, "Forsaken City");
    }

    @Test
    @DisplayName("Without kicker, creatures can attack")
    void withoutKickerCreaturesCanAttack() {
        Permanent bear = addCreatureReady(player1, new AncientSpider());
        castChant(false);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player1.getId()))
                .contains(indexOf(player1, bear));
    }

    @Test
    @DisplayName("With kicker, all creatures can't attack, including one entering later")
    void withKickerCreaturesCantAttack() {
        Permanent existingBear = addCreatureReady(player1, new AncientSpider());
        castChant(true);
        Permanent laterBear = addCreatureReady(player2, new AncientSpider());

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player1.getId()))
                .doesNotContain(indexOf(player1, existingBear));
        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player2.getId()))
                .doesNotContain(indexOf(player2, laterBear));
    }

    @Test
    @DisplayName("The kicker costs an additional white mana")
    void kickerRequiresAdditionalWhiteMana() {
        harness.setHand(player1, List.of(new OrimsChant()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castKickedInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The kicked attack restriction clears at the next turn")
    void kickedRestrictionClearsAtTurnTransition() {
        Permanent bear = addCreatureReady(player2, new AncientSpider());
        castChant(true);

        advanceTurn();

        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player2.getId()))
                .contains(indexOf(player2, bear));
    }

    @Test
    @DisplayName("Chant can target its own caster")
    void canTargetItsOwnCaster() {
        harness.setHand(player1, List.of(new OrimsChant(), new OrimsChant()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The target player can't cast creature spells either")
    void targetPlayerCantCastCreatureSpells() {
        castChant(false);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new AncientSpider()));
        harness.addMana(player2, ManaColor.GREEN, 3);
        harness.addMana(player2, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The casting restriction expires at the end of the turn")
    void castingRestrictionExpires() {
        castChant(false);
        advanceTurn();
        harness.setHand(player2, List.of(new OrimsChant()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertInGraveyard(player2, "Orim's Chant");
    }

    @Test
    @DisplayName("The target player can still activate mana abilities")
    void targetPlayerCanActivateAbilities() {
        harness.addToBattlefield(player2, new ForsakenCity());
        castChant(false);

        harness.activateAbility(player2, 0, null, null);
        harness.handleListChoice(player2, "BLUE");

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(findPermanent(player2, "Forsaken City").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Players can respond to Chant and spells already cast still resolve")
    void responseAndPreviouslyCastSpellStillResolve() {
        harness.setHand(player1, List.of(new OrimsChant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.setHand(player2, List.of(new OrimsChant(), new OrimsChant()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.castInstant(player2, 0, player2.getId());
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.assertInGraveyard(player2, "Orim's Chant");
        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Orim's Chant");
    }

    @Test
    @DisplayName("Kicked Chant does not remove attacking creatures from combat")
    void kickedChantDoesNotRemoveAttackers() {
        Permanent spider = addCreatureReady(player1, new AncientSpider());
        harness.setHand(player2, List.of(new OrimsChant()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(indexOf(player1, spider)));
            harness.castKickedInstant(player2, 0, player1.getId());
            harness.passBothPriorities();
            assertThat(spider.isAttacking()).isTrue();
        });

        resolveCombat(player1);
        harness.assertLife(player2, 18);
    }

    private void castChant(boolean kicked) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new OrimsChant()));
        harness.addMana(player1, ManaColor.WHITE, kicked ? 2 : 1);
        if (kicked) {
            harness.castKickedInstant(player1, 0, player2.getId());
        } else {
            harness.castAndResolveInstant(player1, 0, player2.getId());
            return;
        }
        harness.passBothPriorities();
    }

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
