package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CausticRain;
import com.github.laxika.magicalvibes.cards.e.Electrolyze;
import com.github.laxika.magicalvibes.cards.p.Pyromatics;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinFlectomancer.class, CausticRain.class, GodlessShrine.class,
        Pyromatics.class, GhorClanBloodscale.class, Electrolyze.class})
class GoblinFlectomancerTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Goblin Flectomancer targets a sorcery spell")
    void activationTargetsSorcerySpell() {
        Permanent flectomancer = addCreatureReady(player2, new GoblinFlectomancer());
        Permanent targetLand = harness.addToBattlefieldAndReturn(player2, new GodlessShrine());
        CausticRain causticRain = new CausticRain();
        harness.setHand(player1, List.of(causticRain));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, targetLand.getId());
        harness.passPriority(player1);
        harness.activateAbility(player2, 0, null, causticRain.getId());

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getTargetId()).isEqualTo(causticRain.getId());
        harness.assertInGraveyard(player2, "Goblin Flectomancer");
        assertThat(flectomancer).isNotIn(gd.playerBattlefields.get(player2.getId()));
    }

    @Test
    @DisplayName("Activating Goblin Flectomancer targets an instant spell")
    void activationTargetsInstantSpell() {
        Permanent flectomancer = addCreatureReady(player2, new GoblinFlectomancer());
        Pyromatics pyromatics = new Pyromatics();
        harness.setHand(player1, List.of(pyromatics));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.activateAbility(player2, 0, null, pyromatics.getId());

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getTargetId()).isEqualTo(pyromatics.getId());
        harness.assertInGraveyard(player2, "Goblin Flectomancer");
        assertThat(flectomancer).isNotIn(gd.playerBattlefields.get(player2.getId()));
    }

    @Test
    @DisplayName("Goblin Flectomancer cannot target a creature spell")
    void cannotTargetCreatureSpell() {
        Permanent flectomancer = addCreatureReady(player2, new GoblinFlectomancer());
        GhorClanBloodscale ghorClanBloodscale = new GhorClanBloodscale();
        harness.setHand(player1, List.of(ghorClanBloodscale));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, ghorClanBloodscale.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(flectomancer);
    }

    @Test
    @DisplayName("Accepting Goblin Flectomancer's may ability changes the spell's target")
    void acceptingRetargetChangesSpellTarget() {
        Permanent flectomancer = addCreatureReady(player2, new GoblinFlectomancer());
        Permanent originalTarget = harness.addToBattlefieldAndReturn(player2, new GodlessShrine());
        Permanent newTarget = harness.addToBattlefieldAndReturn(player1, new GodlessShrine());
        CausticRain causticRain = new CausticRain();
        harness.setHand(player1, List.of(causticRain));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, originalTarget.getId());
        harness.passPriority(player1);
        harness.activateAbility(player2, 0, null, causticRain.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, newTarget.getId());

        StackEntry causticRainEntry = gd.stack.stream()
                .filter(entry -> entry.getCard().getId().equals(causticRain.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(causticRainEntry.getTargetId()).isEqualTo(newTarget.getId());

        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(newTarget.getCard().getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(originalTarget);
        harness.assertInGraveyard(player2, "Goblin Flectomancer");
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(flectomancer);
    }

    @Test
    @DisplayName("Declining Goblin Flectomancer's may ability keeps the original target")
    void decliningRetargetKeepsOriginalTarget() {
        Permanent flectomancer = addCreatureReady(player2, new GoblinFlectomancer());
        Permanent originalTarget = harness.addToBattlefieldAndReturn(player2, new GodlessShrine());
        CausticRain causticRain = new CausticRain();
        harness.setHand(player1, List.of(causticRain));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, originalTarget.getId());
        harness.passPriority(player1);
        harness.activateAbility(player2, 0, null, causticRain.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(originalTarget.getCard().getId()));
        harness.assertInGraveyard(player2, "Goblin Flectomancer");
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(flectomancer);
    }

    @Test
    @DisplayName("Changing a multi-target spell changes all targets or none")
    void changingMultiTargetSpellChangesAllTargetsOrNone() {
        addCreatureReady(player2, new GoblinFlectomancer());
        addCreatureReady(player1, new GhorClanBloodscale());
        Electrolyze electrolyze = new Electrolyze();
        harness.setHand(player1, List.of(electrolyze));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, Map.of(player1.getId(), 1, player2.getId(), 1));
        harness.passPriority(player1);
        harness.activateAbility(player2, 0, null, electrolyze.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction())
                .isNotInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
    }
}
