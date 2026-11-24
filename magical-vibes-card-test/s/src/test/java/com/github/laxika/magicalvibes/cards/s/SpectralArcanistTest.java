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
import com.github.laxika.magicalvibes.cards.r.RuleOfLaw;
import com.github.laxika.magicalvibes.cards.t.ThornOfAmethyst;

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

    @Test
    @CardUsed({RuleOfLaw.class})
    @DisplayName("Under Rule of Law the graveyard spell can't be cast after Spectral Arcanist itself, and stays there")
    void ruleOfLawStopsTheFreeCast() {
        var spirit = harness.addToBattlefieldAndReturn(player1, new LanternSpirit());
        spirit.tap();
        harness.addToBattlefield(player2, new RuleOfLaw());
        harness.setGraveyard(player1, List.of(new Vitalize()));
        harness.setHand(player1, List.of(new SpectralArcanist()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).isEmpty();
        assertThat(spirit.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Vitalize");
    }

    @Test
    @CardUsed({ThornOfAmethyst.class})
    @DisplayName("The free cast still pays Thorn of Amethyst's cost increase")
    void freeCastPaysCostIncrease() {
        var spirit = harness.addToBattlefieldAndReturn(player1, new LanternSpirit());
        spirit.tap();
        harness.addToBattlefield(player2, new ThornOfAmethyst());
        Vitalize vitalize = new Vitalize();
        harness.setGraveyard(player1, List.of(vitalize));
        harness.setHand(player1, List.of(new SpectralArcanist()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == vitalize);
    }

    @Test
    @CardUsed({ThornOfAmethyst.class})
    @DisplayName("A free cast whose cost increase can't be paid leaves the card in the graveyard")
    void unpaidCostIncreaseLeavesCardInGraveyard() {
        var spirit = harness.addToBattlefieldAndReturn(player1, new LanternSpirit());
        spirit.tap();
        harness.addToBattlefield(player2, new ThornOfAmethyst());
        harness.setGraveyard(player1, List.of(new Vitalize()));
        harness.setHand(player1, List.of(new SpectralArcanist()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).isEmpty();
        assertThat(spirit.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Vitalize");
    }
}
