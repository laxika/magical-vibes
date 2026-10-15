package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LanternSpirit;
import com.github.laxika.magicalvibes.cards.v.Vitalize;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpectralArcanist.class, Vitalize.class, LanternSpirit.class, GrizzlyBears.class,
        Divination.class})
class SpectralArcanistTest extends BaseCardTest {

    @Test
    @DisplayName("Casts a qualifying instant or sorcery for free and exiles it")
    void castsSpellWithinSpiritCountAndExilesIt() {
        var spirit = harness.addToBattlefieldAndReturn(player1, new LanternSpirit());
        spirit.tap();
        Vitalize vitalize = new Vitalize();
        harness.setGraveyard(player1, List.of(vitalize, new GrizzlyBears()));
        harness.setHand(player1, List.of(new SpectralArcanist()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(spirit.isTapped()).isFalse();
        harness.assertNotInGraveyard(player1, "Vitalize");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.findExiledCard(vitalize.getId())).isNotNull();
    }

    @Test
    @DisplayName("Does not cast a spell above the number of Spirits controlled")
    void doesNotCastSpellAboveSpiritCount() {
        Divination divination = new Divination();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(divination));
        harness.setHand(player1, List.of(new SpectralArcanist()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Divination");
    }
}
