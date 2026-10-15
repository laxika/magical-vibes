package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FlameJavelin;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThroneOfEldraine.class, GrizzlyBears.class, FlameJavelin.class})
class ThroneOfEldraineTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability adds four mana restricted to monocolored spells of the chosen color")
    void tapAbilityAddsRestrictedChosenColorMana() {
        addReadyThrone(CardColor.GREEN);

        harness.activateAbility(player1, 0, 0, null, null);

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.GREEN)).isZero();
        assertThat(pool.getMonocoloredSpellOnlyMana(ManaColor.GREEN)).isEqualTo(4);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears")).isNotNull();
        assertThat(pool.getMonocoloredSpellOnlyMana(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Chosen-color spell-only mana cannot cast a spell of another color")
    void restrictedManaCannotCastAnotherColorSpell() {
        addReadyThrone(CardColor.GREEN);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.setHand(player1, List.of(new FlameJavelin()));

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Draw ability requires mana of the chosen color")
    void drawAbilityRequiresChosenColorMana() {
        addReadyThrone(CardColor.GREEN);
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Entering the battlefield prompts for a color")
    void enteringPromptsForColor() {
        harness.setHand(player1, List.of(new ThroneOfEldraine()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GREEN");

        assertThat(findPermanent(player1, "Throne of Eldraine").getChosenColor())
                .isEqualTo(CardColor.GREEN);
    }

    private Permanent addReadyThrone(CardColor chosenColor) {
        ThroneOfEldraine card = new ThroneOfEldraine();
        Permanent throne = new Permanent(card);
        throne.setSummoningSick(false);
        throne.setChosenColor(chosenColor);
        gd.playerBattlefields.get(player1.getId()).add(throne);
        return throne;
    }
}
