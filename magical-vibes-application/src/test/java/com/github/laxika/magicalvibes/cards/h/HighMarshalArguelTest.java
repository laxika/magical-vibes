package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.ArguelsBloodFast;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HighMarshalArguel.class, ArguelsBloodFast.class})
class HighMarshalArguelTest extends BaseCardTest {

    @Test
    @DisplayName("When High Marshal Arguel dies, it conjures Arguel's Blood Fast and may transform it")
    void deathTriggerConjuresAndMayTransformArguelsBloodFast() {
        Permanent highMarshal = harness.addToBattlefieldAndReturn(player1, new HighMarshalArguel());
        highMarshal.setMarkedDamage(1);
        harness.runStateBasedActions();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Temple of Aclazotz"));
        harness.assertInGraveyard(player1, "High Marshal Arguel");
    }

    @Test
    @DisplayName("If the transform is declined, the conjured Arguel's Blood Fast stays on its front face")
    void decliningTransformLeavesArguelsBloodFastUntransformed() {
        Permanent highMarshal = harness.addToBattlefieldAndReturn(player1, new HighMarshalArguel());
        highMarshal.setMarkedDamage(1);
        harness.runStateBasedActions();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Arguel's Blood Fast"));
    }

    @Test
    @DisplayName("With both Arguel's Blood Fast and Temple of Aclazotz, death creates two flying Vampire Demons instead")
    void matchingPermanentsCreateVampireDemonsInstead() {
        harness.addToBattlefield(player1, new ArguelsBloodFast());
        Permanent temple = harness.addToBattlefieldAndReturn(player1, new ArguelsBloodFast());
        temple.setCard(temple.getCard().getBackFaceCard());
        temple.setTransformed(true);

        Permanent highMarshal = harness.addToBattlefieldAndReturn(player1, new HighMarshalArguel());
        highMarshal.setMarkedDamage(1);
        harness.runStateBasedActions();

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Vampire Demon")).hasSize(2);
        Permanent token = findPermanent(player1, "Vampire Demon");
        assertThat(token.getEffectivePower()).isEqualTo(4);
        assertThat(token.getEffectiveToughness()).isEqualTo(3);
        assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLACK);
        assertThat(token.getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.VAMPIRE, CardSubtype.DEMON);
        assertThat(token.getCard().hasKeyword(Keyword.FLYING)).isTrue();
    }
}
