package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NoviceKnightTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot attack while neither enchanted nor equipped")
    void cannotAttackBare() {
        Permanent knight = setupKnight();

        beginDeclareAttackers();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(indexOf(knight))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Can attack while enchanted by an Aura")
    void canAttackWhileEnchanted() {
        Permanent knight = setupKnight();
        attach(new HolyStrength(), knight);

        beginDeclareAttackers();
        gs.declareAttackers(gd, player1, List.of(indexOf(knight)));

        assertThat(knight.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Can attack while equipped")
    void canAttackWhileEquipped() {
        Permanent knight = setupKnight();
        attach(new LeoninScimitar(), knight);

        beginDeclareAttackers();
        gs.declareAttackers(gd, player1, List.of(indexOf(knight)));

        assertThat(knight.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Cannot attack again once the Aura leaves the battlefield")
    void cannotAttackAfterAuraLeaves() {
        Permanent knight = setupKnight();
        Permanent aura = attach(new HolyStrength(), knight);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        beginDeclareAttackers();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(indexOf(knight))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("An Aura attached to another creature does not let the Knight attack")
    void auraOnAnotherCreatureDoesNotHelp() {
        Permanent knight = setupKnight();
        harness.addToBattlefield(player1, new GrizzlyBears());
        attach(new HolyStrength(), findPermanent(player1, "Grizzly Bears"));

        beginDeclareAttackers();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(indexOf(knight))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    private Permanent setupKnight() {
        harness.addToBattlefield(player1, new NoviceKnight());
        Permanent knight = findPermanent(player1, "Novice Knight");
        knight.setSummoningSick(false);
        harness.addToBattlefield(player2, new GrizzlyBears());
        return knight;
    }

    private Permanent attach(com.github.laxika.magicalvibes.model.Card card, Permanent host) {
        Permanent permanent = new Permanent(card);
        permanent.setAttachedTo(host.getId());
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        return permanent;
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

    private void beginDeclareAttackers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.beginInteraction(new PendingInteraction.AttackerDeclaration(player1.getId()));
    }
}
