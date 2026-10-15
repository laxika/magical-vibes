package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.ChromeshellCrab;
import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.b.Brainstorm;
import com.github.laxika.magicalvibes.cards.d.DemonicCovenant;
import com.github.laxika.magicalvibes.cards.d.DreamEater;
import com.github.laxika.magicalvibes.cards.f.FormlessGenesis;
import com.github.laxika.magicalvibes.cards.n.NetherHorror;
import com.github.laxika.magicalvibes.cards.o.OneWithTheMultiverse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AncientCellarspawn.class, NetherHorror.class, GrizzlyBears.class,
        ChromeshellCrab.class, Counterspell.class, Brainstorm.class, DemonicCovenant.class,
        DreamEater.class, FormlessGenesis.class, OneWithTheMultiverse.class})
class AncientCellarspawnTest extends BaseCardTest {

    @Test
    @DisplayName("Horror spells cost {1} less")
    void matchingCreatureSpellCostsOneLess() {
        harness.addToBattlefield(player1, new AncientCellarspawn());
        harness.setHand(player1, List.of(new NetherHorror()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Nether Horror"));
    }

    @Test
    @DisplayName("A discounted spell makes a target opponent lose the mana-value difference")
    void discountedSpellLosesDifference() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new AncientCellarspawn());
        harness.setHand(player1, List.of(new NetherHorror()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("The trigger does not fire when the spell was not discounted")
    void fullCostSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new AncientCellarspawn());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The cast trigger cannot target its controller")
    void triggerCannotTargetController() {
        harness.addToBattlefield(player1, new AncientCellarspawn());
        harness.setHand(player1, List.of(new NetherHorror()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Nightmare spells are discounted and cause life loss before resolving")
    void nightmareSpellIsDiscounted() {
        harness.addToBattlefield(player1, new AncientCellarspawn());
        harness.setHand(player1, List.of(new DreamEater()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Dream Eater");
    }

    @Test
    @DisplayName("A noncreature kindred Demon spell receives the discount")
    void kindredDemonIsDiscounted() {
        harness.addToBattlefield(player1, new AncientCellarspawn());
        harness.setHand(player1, List.of(new DemonicCovenant()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Changeling matching all three types receives only one discount")
    void changelingDiscountAppliesOnce() {
        harness.addToBattlefield(player1, new AncientCellarspawn());
        harness.setHand(player1, List.of(new FormlessGenesis()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Each Cellarspawn triggers for the combined discount")
    void multipleSourcesStackDiscountsAndTriggers() {
        harness.addToBattlefield(player1, new AncientCellarspawn());
        harness.addToBattlefield(player1, new AncientCellarspawn());
        harness.setHand(player1, List.of(new FormlessGenesis()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        PendingInteraction.ColorChoice order =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(order).isNotNull();
        harness.handleListChoice(player1, order.options().getFirst());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The reduction cannot pay colored mana costs")
    void discountDoesNotReduceColoredMana() {
        harness.addToBattlefield(player1, new AncientCellarspawn());
        harness.addToBattlefield(player1, new AncientCellarspawn());
        harness.setHand(player1, List.of(new AncientCellarspawn()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An opponent's Cellarspawn does not discount your spells or trigger for them")
    void opponentSourceDoesNotApply() {
        harness.addToBattlefield(player2, new AncientCellarspawn());
        harness.setHand(player1, List.of(new AncientCellarspawn()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Casting Cellarspawn itself does not trigger its own ability")
    void sourceDoesNotTriggerForItsOwnCast() {
        harness.setHand(player1, List.of(new AncientCellarspawn()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A face-down spell has mana value zero and must not trigger Cellarspawn")
    void faceDownSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new AncientCellarspawn());
        harness.setHand(player1, List.of(new ChromeshellCrab()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Casting a nontribal spell without paying mana still triggers life loss")
    void freeNontribalSpellTriggers() {
        harness.addToBattlefield(player1, new AncientCellarspawn());
        harness.addToBattlefield(player1, new OneWithTheMultiverse());
        harness.setHand(player1, List.of(new Brainstorm()));

        harness.castInstant(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Countering the discounted spell does not prevent the life loss")
    void triggerSurvivesCounteredSpell() {
        harness.addToBattlefield(player1, new AncientCellarspawn());
        AncientCellarspawn spell = new AncientCellarspawn();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setHand(player2, List.of(new Counterspell()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.castInstant(player2, 0, spell.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Ancient Cellarspawn");
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(gd.stack).isEmpty();
    }
}
