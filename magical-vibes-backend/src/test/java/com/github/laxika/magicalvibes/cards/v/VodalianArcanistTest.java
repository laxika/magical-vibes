package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VodalianArcanistTest extends BaseCardTest {

    // ===== Ability structure =====

    @Test
    @DisplayName("Vodalian Arcanist has one activated ability")
    void hasCorrectAbility() {
        VodalianArcanist card = new VodalianArcanist();

        assertThat(card.getActivatedAbilities()).hasSize(1);

        ActivatedAbility ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost()).isNull();
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(AwardRestrictedManaEffect.class);
    }

    // ===== Mana production =====

    @Test
    @DisplayName("Tap ability adds one instant/sorcery-only colorless mana")
    void tapAbilityAddsRestrictedColorless() {
        harness.addToBattlefield(player1, new VodalianArcanist());

        Permanent arcanist = gd.playerBattlefields.get(player1.getId()).getFirst();
        arcanist.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(0);
        assertThat(gd.playerManaPools.get(player1.getId()).getInstantSorceryOnlyColorless()).isEqualTo(1);
    }

    // ===== Spending restriction: instant/sorcery only =====

    @Test
    @DisplayName("Restricted colorless can pay generic cost of an instant spell")
    void restrictedColorlessPaysForInstantSpell() {
        harness.addToBattlefield(player1, new VodalianArcanist());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent arcanist = gd.playerBattlefields.get(player1.getId()).getFirst();
        arcanist.setSummoningSick(false);

        // Activate ability: 1 instant/sorcery-only colorless
        harness.activateAbility(player1, 0, 0, null, null);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // Shock: {R} — the restricted colorless can't pay for this alone (it's colored cost),
        // but let's use Divination ({2}{U}) instead — 2 generic + 1 blue
        // Pool: 1 instant/sorcery-only colorless + 1 blue + 1 colorless = enough for {2}{U}
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new Divination()));

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Divination resolves — player draws 2 cards
        assertThat(gd.playerManaPools.get(player1.getId()).getInstantSorceryOnlyColorless()).isEqualTo(0);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(0);
    }

    @Test
    @DisplayName("Restricted colorless is not spent when casting a creature spell")
    void restrictedColorlessNotUsedForCreatureSpell() {
        harness.addToBattlefield(player1, new VodalianArcanist());

        Permanent arcanist = gd.playerBattlefields.get(player1.getId()).getFirst();
        arcanist.setSummoningSick(false);

        // Activate ability: 1 instant/sorcery-only colorless
        harness.activateAbility(player1, 0, 0, null, null);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // Cast Grizzly Bears ({1}{G}) with regular mana only
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        // Grizzly Bears enters the battlefield using regular green mana
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        // Instant/sorcery-only colorless should be untouched
        assertThat(gd.playerManaPools.get(player1.getId()).getInstantSorceryOnlyColorless()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot cast creature spell if only restricted colorless available for generic cost")
    void cannotCastCreatureWithOnlyRestrictedColorless() {
        harness.addToBattlefield(player1, new VodalianArcanist());

        Permanent arcanist = gd.playerBattlefields.get(player1.getId()).getFirst();
        arcanist.setSummoningSick(false);

        // Activate ability: 1 instant/sorcery-only colorless
        harness.activateAbility(player1, 0, 0, null, null);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // Try to cast Grizzly Bears ({1}{G}) with only 1 green + 1 restricted colorless
        // Should fail because restricted colorless can't pay for creature spells
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Restricted colorless drains at phase transition")
    void restrictedColorlessDrainsAtPhaseTransition() {
        harness.addToBattlefield(player1, new VodalianArcanist());

        Permanent arcanist = gd.playerBattlefields.get(player1.getId()).getFirst();
        arcanist.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).getInstantSorceryOnlyColorless()).isEqualTo(1);

        // Drain non-persistent mana (simulates phase transition)
        gd.playerManaPools.get(player1.getId()).drainNonPersistent();

        assertThat(gd.playerManaPools.get(player1.getId()).getInstantSorceryOnlyColorless()).isEqualTo(0);
    }
}
