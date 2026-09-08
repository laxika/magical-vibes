package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VodalianMystic.class, Shock.class, GrizzlyBears.class})
class VodalianMysticTest extends BaseCardTest {

    @Test
    @DisplayName("Target instant spell becomes the chosen color")
    void targetInstantBecomesChosenColor() {
        addReadyMystic(player1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        Card targetSpell = gd.stack.getFirst().getCard();

        harness.activateAbility(player1, 0, 0, null, targetSpell.getId(), Zone.STACK);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "BLUE");

        assertThat(gqs.getEffectiveCardColors(gd, targetSpell)).containsExactly(CardColor.BLUE);
    }

    @Test
    @DisplayName("The ability cannot target a creature spell")
    void cannotTargetCreatureSpell() {
        addReadyMystic(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        UUID creatureSpellId = gd.stack.getFirst().getCard().getId();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 0, null, creatureSpellId, Zone.STACK))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyMystic(Player player) {
        Permanent mystic = new Permanent(new VodalianMystic());
        mystic.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(mystic);
        return mystic;
    }
}
