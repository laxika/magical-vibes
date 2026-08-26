package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(InquisitivePuppet.class)
class InquisitivePuppetTest extends BaseCardTest {

    @Test
    void enteringTheBattlefieldTriggersScryOne() {
        harness.setHand(player1, List.of(new InquisitivePuppet()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(1);
    }

    @Test
    void exilingItselfCreatesAWhiteHumanToken() {
        Permanent puppet = new Permanent(new InquisitivePuppet());
        puppet.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(puppet);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Inquisitive Puppet");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Inquisitive Puppet"));

        Permanent human = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Human"))
                .findFirst()
                .orElseThrow();
        assertThat(human.getCard().getSubtypes()).containsExactly(CardSubtype.HUMAN);
        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, human)).isEqualTo(1);
    }
}
