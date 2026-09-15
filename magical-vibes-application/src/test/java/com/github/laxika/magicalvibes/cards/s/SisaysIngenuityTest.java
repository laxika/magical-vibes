package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.ManaCylix;
import com.github.laxika.magicalvibes.cards.m.MoggSentry;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SisaysIngenuity.class, MoggSentry.class, ManaCylix.class})
class SisaysIngenuityTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Sisay's Ingenuity attaches to a creature and draws a card")
    void resolvingAttachesAndDraws() {
        Permanent enchantedCreature = harness.addToBattlefieldAndReturn(player1, new MoggSentry());
        harness.setHand(player1, List.of(new SisaysIngenuity()));
        harness.setLibrary(player1, List.of(new SisaysIngenuity()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.castEnchantment(player1, 0, List.of(enchantedCreature.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() instanceof SisaysIngenuity
                        && p.isAttached()
                        && enchantedCreature.getId().equals(p.getAttachedTo()));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Enchanted creature can pay to make a target creature blue")
    void grantedAbilityChangesTargetCreatureColor() {
        Permanent enchantedCreature = addCreatureReady(player1, new MoggSentry());
        Permanent target = addCreatureReady(player2, new MoggSentry());
        attachAuraTo(player1, enchantedCreature);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.BLUE);
    }

    @Test
    @DisplayName("Color change from the granted ability wears off at end of turn")
    void colorChangeWearsOffAtEndOfTurn() {
        Permanent enchantedCreature = addCreatureReady(player1, new MoggSentry());
        Permanent target = addCreatureReady(player2, new MoggSentry());
        attachAuraTo(player1, enchantedCreature);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        target.resetModifiers();
        gd.expireEndOfTurnFloatingEffects();

        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.RED);
    }

    @Test
    @DisplayName("Granted ability cannot target a noncreature permanent")
    void grantedAbilityRejectsNoncreatureTarget() {
        Permanent enchantedCreature = addCreatureReady(player1, new MoggSentry());
        Permanent manaCylix = harness.addToBattlefieldAndReturn(player2, new ManaCylix());
        attachAuraTo(player1, enchantedCreature);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, manaCylix.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The enchanted creature's controller controls the granted ability")
    void enchantedCreatureControllerActivatesGrantedAbility() {
        Permanent enchantedCreature = addCreatureReady(player2, new MoggSentry());
        Permanent target = addCreatureReady(player1, new MoggSentry());
        attachAuraTo(player1, enchantedCreature);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateAbility(player2, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleListChoice(player2, "WHITE");

        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.WHITE);
    }

    private void attachAuraTo(com.github.laxika.magicalvibes.model.Player auraController,
                              Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(auraController, new SisaysIngenuity());
        aura.setAttachedTo(creature.getId());
    }
}
