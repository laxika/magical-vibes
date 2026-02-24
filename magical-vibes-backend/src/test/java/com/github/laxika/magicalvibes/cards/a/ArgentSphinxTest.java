package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnAtEndStepEffect;
import com.github.laxika.magicalvibes.service.GameQueryService;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArgentSphinxTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameService gs;
    private GameQueryService gqs;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gs = harness.getGameService();
        gqs = harness.getGameQueryService();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Argent Sphinx has metalcraft activated ability")
    void hasMetalcraftAbility() {
        ArgentSphinx card = new ArgentSphinx();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        ActivatedAbility ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.getManaCost()).isEqualTo("{U}");
        assertThat(ability.isRequiresTap()).isFalse();
        assertThat(ability.isNeedsTarget()).isFalse();
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(ExileSelfAndReturnAtEndStepEffect.class);
        assertThat(ability.getTimingRestriction()).isEqualTo(ActivationTimingRestriction.METALCRAFT);
    }

    // ===== Metalcraft restriction =====

    @Test
    @DisplayName("Cannot activate ability without three artifacts")
    void cannotActivateWithoutThreeArtifacts() {
        addSphinxReady(player1);
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Metalcraft");
    }

    @Test
    @DisplayName("Can activate ability with three artifacts")
    void canActivateWithThreeArtifacts() {
        addSphinxReady(player1);
        addThreeArtifacts(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Sphinx should be exiled
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Argent Sphinx"));
        assertThat(gd.playerExiledCards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Argent Sphinx"));
    }

    // ===== Exile and return =====

    @Test
    @DisplayName("Sphinx is exiled when ability resolves")
    void sphinxIsExiledOnResolve() {
        addSphinxReady(player1);
        addThreeArtifacts(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Argent Sphinx"));
        assertThat(gd.playerExiledCards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Argent Sphinx"));
    }

    @Test
    @DisplayName("Sphinx returns to battlefield at beginning of next end step")
    void sphinxReturnsAtEndStep() {
        addSphinxReady(player1);
        addThreeArtifacts(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Sphinx is exiled
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Argent Sphinx"));

        // Advance to end step naturally (POSTCOMBAT_MAIN -> END_STEP triggers handler)
        advanceToEndStep();

        // Sphinx should be back on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Argent Sphinx"));
        // And removed from exile
        assertThat(gd.playerExiledCards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Argent Sphinx"));
    }

    @Test
    @DisplayName("Returned Sphinx has summoning sickness")
    void returnedSphinxHasSummoningSickness() {
        addSphinxReady(player1);
        addThreeArtifacts(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        advanceToEndStep();

        Permanent returnedSphinx = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Argent Sphinx"))
                .findFirst().orElseThrow();
        assertThat(returnedSphinx.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("Returned Sphinx is under controller's control")
    void returnedSphinxUnderControllerControl() {
        addSphinxReady(player1);
        addThreeArtifacts(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        advanceToEndStep();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Argent Sphinx"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Argent Sphinx"));
    }

    // ===== Mana cost =====

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        addSphinxReady(player1);
        addThreeArtifacts(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Mana is consumed when ability is activated")
    void manaConsumedOnActivation() {
        addSphinxReady(player1);
        addThreeArtifacts(player1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    // ===== Helpers =====

    private Permanent addSphinxReady(Player player) {
        Permanent perm = new Permanent(new ArgentSphinx());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void addThreeArtifacts(Player player) {
        harness.addToBattlefield(player, new Spellbook());
        harness.addToBattlefield(player, new LeoninScimitar());
        harness.addToBattlefield(player, new Spellbook());
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances POSTCOMBAT_MAIN -> END_STEP
    }
}
