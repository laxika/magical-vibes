package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.CostOfBrilliance;
import com.github.laxika.magicalvibes.cards.f.Fireblast;
import com.github.laxika.magicalvibes.cards.m.ManOWar;
import com.github.laxika.magicalvibes.cards.s.SoulOfShandalar;
import com.github.laxika.magicalvibes.cards.w.Warthog;
import com.github.laxika.magicalvibes.cards.z.ZhalfirinCrusader;
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

@CardUsed({PeaceTalks.class, Warthog.class, Fireblast.class, ZhalfirinCrusader.class, ManOWar.class,
        CostOfBrilliance.class, SoulOfShandalar.class})
class PeaceTalksTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures can't attack this turn after Peace Talks resolves")
    void creaturesCantAttackThisTurn() {
        Permanent warthog = addCreatureReady(player1, new Warthog());
        castPeaceTalks(player1);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player1.getId())).isEmpty();
        assertThat(gd.peaceTalksTurnsRemaining).isEqualTo(2);
        assertThat(warthog.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Restriction lasts through the next turn, then clears")
    void lastsThisTurnAndNextThenClears() {
        Permanent warthog = addCreatureReady(player1, new Warthog());
        castPeaceTalks(player1);
        assertThat(gd.peaceTalksTurnsRemaining).isEqualTo(2);

        // Leave player1's turn → still active on player2's turn (1 remaining)
        advanceTurn();
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gd.peaceTalksTurnsRemaining).isEqualTo(1);

        Permanent oppWarthog = addCreatureReady(player2, new Warthog());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player2.getId())).isEmpty();
        assertThat(oppWarthog.isTapped()).isFalse();

        // Leave player2's turn → Peace Talks expires
        advanceTurn();
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.peaceTalksTurnsRemaining).isEqualTo(0);
        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player1.getId()))
                .contains(indexOf(player1, warthog));
    }

    @Test
    @DisplayName("Spells cannot target permanents while Peace Talks is active")
    void spellsCannotTargetPermanents() {
        Permanent warthog = addCreatureReady(player2, new Warthog());
        castPeaceTalks(player1);

        harness.setHand(player1, List.of(new Fireblast()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        assertThatThrownBy(() -> harness.castInstant(player1, 0, warthog.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be the targets");
    }

    @Test
    @DisplayName("Spells cannot target players while Peace Talks is active")
    void spellsCannotTargetPlayers() {
        castPeaceTalks(player1);

        harness.setHand(player1, List.of(new Fireblast()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be the targets");
    }

    @Test
    @DisplayName("Activated abilities cannot target permanents while Peace Talks is active")
    void activatedAbilitiesCannotTargetPermanents() {
        Permanent victim = addCreatureReady(player1, new Warthog());
        Permanent crusader = addCreatureReady(player2, new ZhalfirinCrusader());
        castPeaceTalks(player1);

        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> harness.activateAbility(player2, indexOf(player2, crusader), null, victim.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be the targets");
    }

    @Test
    @DisplayName("Activated abilities cannot target players while Peace Talks is active")
    void activatedAbilitiesCannotTargetPlayers() {
        Permanent crusader = addCreatureReady(player2, new ZhalfirinCrusader());
        castPeaceTalks(player1);

        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> harness.activateAbility(player2, indexOf(player2, crusader), null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be the targets");
    }

    @Test
    @DisplayName("Triggered abilities can target permanents while Peace Talks is active")
    void triggeredAbilitiesCanTargetPermanents() {
        Permanent warthog = addCreatureReady(player2, new Warthog());
        castPeaceTalks(player1);

        harness.setHand(player1, List.of(new ManOWar()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, warthog.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Warthog");
        harness.assertInHand(player2, "Warthog");
    }

    @Test
    @DisplayName("Multi-target spells cannot target players while Peace Talks is active")
    void multiTargetSpellsCannotTargetPlayers() {
        castPeaceTalks(player1);

        harness.setHand(player1, List.of(new CostOfBrilliance()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be the targets");
    }

    @Test
    @DisplayName("Multi-target activated abilities cannot target players while Peace Talks is active")
    void multiTargetActivatedAbilitiesCannotTargetPlayers() {
        Permanent soul = addCreatureReady(player1, new SoulOfShandalar());
        castPeaceTalks(player1);

        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, indexOf(player1, soul), 0, List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be the targets");
    }

    private void castPeaceTalks(Player caster) {
        harness.forceActivePlayer(caster);
        harness.castFromHand(caster, new PeaceTalks(), "{1}{W}");
        harness.passBothPriorities();
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();
    }
}
