package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.h.HornedTurtle;
import com.github.laxika.magicalvibes.cards.s.SoltariFootSoldier;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThalakosMistfolk.class, HornedTurtle.class, SoltariFootSoldier.class})
class ThalakosMistfolkTest extends BaseCardTest {

    @Test
    @DisplayName("Activating {U} puts Thalakos Mistfolk on top of its owner's library")
    void activateAbilityPutsOnTopOfLibrary() {
        Permanent mistfolk = harness.addToBattlefieldAndReturn(player1, new ThalakosMistfolk());
        mistfolk.setSummoningSick(false);
        HornedTurtle existingTopCard = new HornedTurtle();
        harness.setLibrary(player1, List.of(existingTopCard));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(mistfolk);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(mistfolk.getCard());
        assertThat(gd.playerDecks.get(player1.getId()).get(0)).isSameAs(mistfolk.getCard());
        assertThat(gd.playerDecks.get(player1.getId()).get(1)).isSameAs(existingTopCard);
    }

    @Test
    @DisplayName("Ability can be activated the turn Thalakos Mistfolk enters (no tap cost)")
    void abilityUsableWhileSummoningSick() {
        Permanent mistfolk = harness.addToBattlefieldAndReturn(player1, new ThalakosMistfolk());
        mistfolk.setSummoningSick(true);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(mistfolk.getCard());
    }

    @Test
    @DisplayName("Shadow prevents a creature without shadow from blocking Thalakos Mistfolk")
    void shadowPreventsNonShadowBlocker() {
        Permanent mistfolk = addCreatureReady(player1, new ThalakosMistfolk());
        mistfolk.setAttacking(true);
        addCreatureReady(player2, new HornedTurtle());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Shadow allows a shadow creature to block Thalakos Mistfolk")
    void shadowAllowsShadowBlocker() {
        Permanent mistfolk = addCreatureReady(player1, new ThalakosMistfolk());
        mistfolk.setAttacking(true);
        Permanent shadowBlocker = addCreatureReady(player2, new SoltariFootSoldier());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(shadowBlocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Shadow prevents Thalakos Mistfolk from blocking a creature without shadow")
    void shadowPreventsBlockingNonShadowAttacker() {
        addCreatureReady(player1, new HornedTurtle()).setAttacking(true);
        addCreatureReady(player2, new ThalakosMistfolk());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }
}
