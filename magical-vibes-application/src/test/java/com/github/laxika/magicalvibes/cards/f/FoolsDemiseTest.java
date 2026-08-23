package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
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

@CardUsed({FoolsDemise.class, DoomBlade.class, GrizzlyBears.class, Naturalize.class})
class FoolsDemiseTest extends BaseCardTest {

    @Test
    @DisplayName("When the enchanted creature dies, it returns under the Aura controller's control")
    void returnsCreatureUnderAuraControllersControl() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Card creatureCard = creature.getCard();

        castFoolsDemise(player1, creature);
        killCreature(player1, creature);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creatureCard.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(creatureCard.getId()));
        harness.assertInHand(player1, "Fool's Demise");
        harness.assertNotInGraveyard(player1, "Fool's Demise");
    }

    @Test
    @DisplayName("When Fool's Demise is put into a graveyard from the battlefield, it returns to its owner's hand")
    void returnsToHandWhenAuraIsDestroyed() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        castFoolsDemise(player1, creature);
        Permanent aura = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Fool's Demise"))
                .findFirst()
                .orElseThrow();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castInstant(player2, 0, aura.getId());
        resolveStackFully();

        harness.assertInHand(player1, "Fool's Demise");
        harness.assertNotInGraveyard(player1, "Fool's Demise");
        harness.assertNotOnBattlefield(player1, "Fool's Demise");
    }

    @Test
    @DisplayName("Fool's Demise cannot enchant a non-creature permanent")
    void cannotEnchantNonCreature() {
        Permanent nonCreature = new Permanent(new FoolsDemise());
        gd.playerBattlefields.get(player2.getId()).add(nonCreature);

        harness.setHand(player1, List.of(new FoolsDemise()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castFoolsDemise(Player controller, Permanent target) {
        harness.setHand(controller, List.of(new FoolsDemise()));
        harness.addMana(controller, ManaColor.BLUE, 5);
        harness.castEnchantment(controller, 0, target.getId());
        harness.passBothPriorities();
    }

    private void killCreature(Player caster, Permanent creature) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new DoomBlade()));
        harness.addMana(caster, ManaColor.BLACK, 2);
        harness.castInstant(caster, 0, creature.getId());
        resolveStackFully();
    }

    private void resolveStackFully() {
        for (int i = 0; i < 8 && (!gd.stack.isEmpty() || !gd.pendingManaAbilityTriggers.isEmpty()); i++) {
            harness.passBothPriorities();
        }
    }
}
