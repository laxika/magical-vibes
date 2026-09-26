package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PestRescuer.class, ProfessorOfZoomancy.class, Shock.class})
class PestRescuerTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Pest during an upkeep and its death gains two life")
    void createsPestAndAdditionalLifeGainAppliesToItsDeathTrigger() {
        harness.addToBattlefield(player1, new PestRescuer());
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        Permanent pest = pestTokens(player1).getFirst();
        assertThat(pest.getCard().getSubtypes()).contains(CardSubtype.PEST);
        assertThat(pest.getCard().isToken()).isTrue();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, pest.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Does not create another Pest while a Pest token is controlled")
    void doesNotCreateAnotherPestWhileTokenIsControlled() {
        harness.addToBattlefield(player1, new PestRescuer());
        harness.enterBattlefieldAndReturn(player1, new ProfessorOfZoomancy());
        resolveAllTriggers();

        advanceToUpkeep(player2);
        assertThat(gd.stack).isEmpty();
        assertThat(pestTokens(player1)).hasSize(1);
    }

    @Test
    @DisplayName("Rechecks the no-Pest-token condition when the trigger resolves")
    void rechecksConditionAtResolution() {
        harness.addToBattlefield(player1, new PestRescuer());

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);

        harness.enterBattlefieldAndReturn(player1, new ProfessorOfZoomancy());
        resolveAllTriggers();

        assertThat(pestTokens(player1)).hasSize(1);
    }

    @Test
    @DisplayName("Additional life gain only applies to the controller")
    void additionalLifeGainOnlyAppliesToController() {
        harness.addToBattlefield(player1, new PestRescuer());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 3));
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player2.getId(), 3));

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 23);
    }

    private List<Permanent> pestTokens(Player player) {
        return findPermanents(player, "Pest").stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.PEST))
                .toList();
    }
}
