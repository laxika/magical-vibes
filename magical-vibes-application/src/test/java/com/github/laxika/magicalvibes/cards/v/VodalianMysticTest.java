package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.Dodecapod;
import com.github.laxika.magicalvibes.cards.i.Index;
import com.github.laxika.magicalvibes.cards.p.PropheticBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VodalianMystic.class, PropheticBolt.class, Index.class, Dodecapod.class})
class VodalianMysticTest extends BaseCardTest {

    @Test
    @DisplayName("Target instant spell becomes the chosen color")
    void targetInstantBecomesChosenColor() {
        Permanent mystic = addCreatureReady(player1, new VodalianMystic());
        harness.setHand(player1, List.of(new PropheticBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, player2.getId());
        Card targetSpell = gd.stack.getFirst().getCard();

        harness.activateAbility(player1, 0, 0, null, targetSpell.getId(), Zone.STACK);
        assertThat(mystic.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "BLUE");

        assertThat(gqs.getEffectiveCardColors(gd, targetSpell)).containsExactly(CardColor.BLUE);
    }

    @Test
    @DisplayName("Target sorcery spell becomes the chosen color")
    void targetSorceryBecomesChosenColor() {
        addCreatureReady(player1, new VodalianMystic());
        Card targetSpell = new Index();
        harness.castFromHand(player1, targetSpell, "{U}");

        harness.activateAbility(player1, 0, 0, null, targetSpell.getId(), Zone.STACK);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "RED");

        assertThat(gqs.getEffectiveCardColors(gd, targetSpell)).containsExactly(CardColor.RED);
    }

    @Test
    @DisplayName("The ability cannot target a creature spell")
    void cannotTargetCreatureSpell() {
        addCreatureReady(player1, new VodalianMystic());
        Card creatureSpell = new Dodecapod();
        harness.castFromHand(player1, creatureSpell, "{4}");

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 0, null, creatureSpell.getId(), Zone.STACK))
                .isInstanceOf(IllegalStateException.class);
    }

}
