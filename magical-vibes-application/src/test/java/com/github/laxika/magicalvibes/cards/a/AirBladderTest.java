package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.Cloudskate;
import com.github.laxika.magicalvibes.cards.r.RootwaterCommando;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AirBladder.class, Cloudskate.class, RootwaterCommando.class})
class AirBladderTest extends BaseCardTest {

    @Test
    void resolvingAirBladderAttachesToTargetCreature() {
        Permanent target = addCreatureReady(player1, new RootwaterCommando());

        harness.setHand(player1, List.of(new AirBladder()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        gs.playCard(gd, player1, 0, 0, target.getId(), null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof AirBladder
                        && permanent.isAttached()
                        && permanent.getAttachedTo().equals(target.getId()));
    }

    @Test
    void enchantedCreatureHasFlying() {
        Permanent target = addCreatureReady(player1, new RootwaterCommando());

        Permanent aura = new Permanent(new AirBladder());
        aura.setAttachedTo(target.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
    }

    @Test
    void enchantedCreatureCanBlockFlyingCreature() {
        Permanent blocker = enchantedCreatureOnBattlefield(player2);

        Permanent attacker = addCreatureReady(player1, new Cloudskate());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void enchantedCreatureCannotBlockCreatureWithoutFlying() {
        enchantedCreatureOnBattlefield(player2);

        Permanent attacker = addCreatureReady(player1, new RootwaterCommando());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only block creatures with flying");
    }

    @Test
    void blockRestrictionEndsWhenAuraLeaves() {
        Permanent blocker = enchantedCreatureOnBattlefield(player2);
        Permanent aura = findPermanent(player2, "Air Bladder");
        Permanent attacker = addCreatureReady(player1, new RootwaterCommando());
        attacker.setAttacking(true);

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void canEnchantOpponentsCreature() {
        Permanent target = addCreatureReady(player2, new RootwaterCommando());
        harness.setHand(player1, List.of(new AirBladder()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Air Bladder").getAttachedTo()).isEqualTo(target.getId());
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();

        Permanent attacker = addCreatureReady(player1, new RootwaterCommando());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only block creatures with flying");
    }

    @Test
    void unenchantedCreatureCanStillBlockNonflyingAttacker() {
        enchantedCreatureOnBattlefield(player2);
        Permanent blocker = addCreatureReady(player2, new RootwaterCommando());
        Permanent attacker = addCreatureReady(player1, new RootwaterCommando());
        attacker.setAttacking(true);

        assertThat(gqs.hasKeyword(gd, blocker, Keyword.FLYING)).isFalse();
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(2, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void canBlockAttackerWithFlyingGrantedByAnotherAirBladder() {
        Permanent blocker = enchantedCreatureOnBattlefield(player2);
        Permanent attacker = enchantedCreatureOnBattlefield(player1);
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent enchantedCreatureOnBattlefield(Player player) {
        Permanent blocker = addCreatureReady(player, new RootwaterCommando());

        Permanent aura = new Permanent(new AirBladder());
        aura.setAttachedTo(blocker.getId());
        gd.playerBattlefields.get(player.getId()).add(aura);
        return blocker;
    }
}
