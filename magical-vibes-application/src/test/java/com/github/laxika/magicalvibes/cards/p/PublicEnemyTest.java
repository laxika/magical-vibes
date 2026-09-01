package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({PublicEnemy.class, GrizzlyBears.class, DoomBlade.class})
class PublicEnemyTest extends BaseCardTest {

    @Test
    @DisplayName("All creatures that can attack the enchanted controller must attack")
    void allEligibleCreaturesMustAttackEnchantedController() {
        Permanent enchanted = addCreatureReady(player1, new GrizzlyBears());
        addAura(player1, enchanted);
        addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> declareAttackers(player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Creatures attack the enchanted creature controller when able")
    void eligibleCreatureCanAttackEnchantedController() {
        Permanent enchanted = addCreatureReady(player1, new GrizzlyBears());
        addAura(player1, enchanted);
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));

        assertThat(gd.playerBattlefields.get(player2.getId()).get(0).isAttacking()).isTrue();
    }

    @Test
    @DisplayName("The enchanted creature's controller draws when it dies")
    void drawsWhenEnchantedCreatureDies() {
        Permanent enchanted = addCreatureReady(player1, new GrizzlyBears());
        addAura(player2, enchanted);
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new GrizzlyBears()));

        destroyWithDoomBlade(player2, enchanted);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    private void destroyWithDoomBlade(Player caster, Permanent target) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new DoomBlade()));
        harness.addMana(caster, ManaColor.BLACK, 2);
        harness.castInstant(caster, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addAura(Player auraController, Permanent enchanted) {
        Card auraCard = new PublicEnemy();
        Permanent aura = new Permanent(auraCard);
        aura.setAttachedTo(enchanted.getId());
        gd.playerBattlefields.get(auraController.getId()).add(aura);
    }
}
