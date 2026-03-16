package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FleshEaterImpTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has one activated ability with SacrificeCreatureCost and BoostSelfEffect(1,1)")
    void hasCorrectAbilityStructure() {
        FleshEaterImp card = new FleshEaterImp();

        assertThat(card.getActivatedAbilities()).hasSize(1);

        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isFalse();
        assertThat(ability.getManaCost()).isNull();
        assertThat(ability.isNeedsTarget()).isFalse();
        assertThat(ability.getEffects()).hasSize(2);
        assertThat(ability.getEffects().get(0)).isInstanceOf(SacrificeCreatureCost.class);
        assertThat(ability.getEffects().get(1)).isInstanceOf(BoostSelfEffect.class);

        BoostSelfEffect boost = (BoostSelfEffect) ability.getEffects().get(1);
        assertThat(boost.powerBoost()).isEqualTo(1);
        assertThat(boost.toughnessBoost()).isEqualTo(1);
    }

    // ===== Activation: sacrificing a creature =====

    @Test
    @DisplayName("Activating ability sacrifices the chosen creature and puts boost on the stack")
    void activatingAbilitySacrificesCreatureAndPutsBoostOnStack() {
        Permanent impPerm = addFleshEaterImpReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bearsId);

        // Grizzly Bears should be sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // Flesh-Eater Imp should still be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Flesh-Eater Imp"));

        // Ability should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Flesh-Eater Imp");
        assertThat(gd.stack.getFirst().getTargetPermanentId()).isEqualTo(impPerm.getId());
        assertThat(gd.stack.getFirst().isNonTargeting()).isTrue();
    }

    @Test
    @DisplayName("Resolving ability gives Flesh-Eater Imp +1/+1")
    void resolvingAbilityBoostsImp() {
        addFleshEaterImpReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        Permanent imp = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(imp.getCard().getName()).isEqualTo("Flesh-Eater Imp");
        assertThat(imp.getPowerModifier()).isEqualTo(1);
        assertThat(imp.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can activate multiple times by sacrificing different creatures")
    void canActivateMultipleTimes() {
        addFleshEaterImpReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, createTokenCreature("Saproling Token"));

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();

        UUID tokenId = harness.getPermanentId(player1, "Saproling Token");
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, tokenId);
        harness.passBothPriorities();

        Permanent imp = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(imp.getCard().getName()).isEqualTo("Flesh-Eater Imp");
        assertThat(imp.getPowerModifier()).isEqualTo(2);
        assertThat(imp.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Can sacrifice Flesh-Eater Imp to its own ability")
    void canSacrificeItself() {
        addFleshEaterImpReady(player1);

        harness.activateAbility(player1, 0, null, null);

        // Imp should be sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Flesh-Eater Imp"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Flesh-Eater Imp"));

        // Ability should still be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Flesh-Eater Imp");
    }

    // ===== Boost resets at end of turn =====

    @Test
    @DisplayName("Boost resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        addFleshEaterImpReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();

        Permanent imp = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(imp.getPowerModifier()).isEqualTo(1);
        assertThat(imp.getToughnessModifier()).isEqualTo(1);

        // Advance to cleanup step
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(imp.getPowerModifier()).isEqualTo(0);
        assertThat(imp.getToughnessModifier()).isEqualTo(0);
    }

    // ===== No mana cost =====

    @Test
    @DisplayName("Ability has no mana cost — can activate without mana")
    void canActivateWithoutMana() {
        addFleshEaterImpReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bearsId);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Can activate ability even when Imp is tapped")
    void canActivateWhenTapped() {
        Permanent impPerm = addFleshEaterImpReady(player1);
        impPerm.tap();
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bearsId);

        assertThat(gd.stack).hasSize(1);
    }

    // ===== Auto-sacrifice =====

    @Test
    @DisplayName("When Imp is the only creature, it auto-sacrifices itself")
    void autoSacrificesWhenOnlyCreature() {
        addFleshEaterImpReady(player1);

        harness.activateAbility(player1, 0, null, null);

        // Imp should be auto-sacrificed (only creature available)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Flesh-Eater Imp"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Flesh-Eater Imp"));

        // Ability should still be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Flesh-Eater Imp");
    }

    // ===== Helper methods =====

    private Permanent addFleshEaterImpReady(Player player) {
        FleshEaterImp card = new FleshEaterImp();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Card createTokenCreature(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{G}");
        card.setColor(CardColor.GREEN);
        card.setPower(1);
        card.setToughness(1);
        return card;
    }
}
