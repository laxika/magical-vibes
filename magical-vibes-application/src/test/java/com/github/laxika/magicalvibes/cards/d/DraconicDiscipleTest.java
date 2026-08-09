package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DraconicDiscipleTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Draconic Disciple prompts for an any-color mana choice")
    void tappingProducesAnyColorMana() {
        Permanent disciple = harness.addToBattlefieldAndReturn(player1, new DraconicDisciple());
        disciple.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(disciple.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Paying seven mana and sacrificing Draconic Disciple creates a 5/5 flying Dragon")
    void sacrificesToCreateDragonToken() {
        Permanent disciple = harness.addToBattlefieldAndReturn(player1, new DraconicDisciple());
        disciple.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertNotOnBattlefield(player1, "Draconic Disciple");
        harness.passBothPriorities();

        List<Permanent> dragons = findPermanents(player1, "Dragon");
        assertThat(dragons).hasSize(1);
        Permanent dragon = dragons.getFirst();
        assertThat(gqs.getEffectivePower(gd, dragon)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, dragon)).isEqualTo(5);
        assertThat(dragon.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(dragon.getCard().getSubtypes()).contains(CardSubtype.DRAGON);
        assertThat(dragon.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(dragon.getCard().isToken()).isTrue();
    }
}
