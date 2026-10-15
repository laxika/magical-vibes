package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NoviceKnight.class, GrizzlyBears.class, HolyStrength.class, LeoninScimitar.class})
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

    @Test
    @DisplayName("Cannot attack once its Equipment is detached")
    void cannotAttackAfterEquipmentDetaches() {
        Permanent knight = setupKnight();
        Permanent equipment = attach(new LeoninScimitar(), knight);
        equipment.setAttachedTo(null);

        beginDeclareAttackers();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(indexOf(knight))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Can still attack after losing its Aura if it remains equipped")
    void canAttackWithEquipmentAfterAuraLeaves() {
        Permanent knight = setupKnight();
        Permanent aura = attach(new HolyStrength(), knight);
        attach(new LeoninScimitar(), knight);
        gd.playerBattlefields.get(player1.getId()).remove(aura);

        beginDeclareAttackers();
        gs.declareAttackers(gd, player1, List.of(indexOf(knight)));

        assertThat(knight.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Can still attack after Equipment detaches if it remains enchanted")
    void canAttackWithAuraAfterEquipmentDetaches() {
        Permanent knight = setupKnight();
        attach(new HolyStrength(), knight);
        Permanent equipment = attach(new LeoninScimitar(), knight);
        equipment.setAttachedTo(null);

        beginDeclareAttackers();
        gs.declareAttackers(gd, player1, List.of(indexOf(knight)));

        assertThat(knight.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("An opponent-controlled Aura also permits attacking")
    void canAttackWithOpponentControlledAura() {
        Permanent knight = setupKnight();
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new HolyStrength());
        aura.setAttachedTo(knight.getId());

        beginDeclareAttackers();
        gs.declareAttackers(gd, player1, List.of(indexOf(knight)));

        assertThat(knight.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Being equipped does not bypass summoning sickness")
    void cannotAttackWhileSummoningSickAndEquipped() {
        Permanent knight = setupKnight();
        knight.setSummoningSick(true);
        attach(new LeoninScimitar(), knight);

        beginDeclareAttackers();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(indexOf(knight))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Being enchanted does not permit attacking while tapped")
    void cannotAttackWhileTappedAndEnchanted() {
        Permanent knight = setupKnight();
        knight.tap();
        attach(new HolyStrength(), knight);

        beginDeclareAttackers();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(indexOf(knight))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    private Permanent setupKnight() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new NoviceKnight());
        knight.setSummoningSick(false);
        harness.addToBattlefield(player2, new GrizzlyBears());
        return knight;
    }

    private Permanent attach(com.github.laxika.magicalvibes.model.Card card, Permanent host) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, card);
        permanent.setAttachedTo(host.getId());
        return permanent;
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

    private void beginDeclareAttackers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }
}
