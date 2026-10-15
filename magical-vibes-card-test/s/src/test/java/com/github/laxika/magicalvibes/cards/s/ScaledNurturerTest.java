package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScaledNurturer.class})
class ScaledNurturerTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Scaled Nurturer adds green mana")
    void tappingAddsGreenMana() {
        Permanent nurturer = addReadyNurturer();

        harness.activateAbility(player1, 0, null, null);

        assertThat(manaPool().get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(nurturer.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Mana spent on a Dragon creature spell gains 2 life")
    void dragonCastWithNurturerManaGainsLife() {
        addReadyNurturer();
        harness.activateAbility(player1, 0, null, null);
        harness.setHand(player1, List.of(createCreature("Test Dragon", CardSubtype.DRAGON)));

        int lifeBefore = gd.getLife(player1.getId());
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("Mana spent on a non-Dragon creature spell does not gain life")
    void nonDragonCastWithNurturerManaDoesNotGainLife() {
        addReadyNurturer();
        harness.activateAbility(player1, 0, null, null);
        harness.setHand(player1, List.of(createCreature("Test Goblin", CardSubtype.GOBLIN)));

        int lifeBefore = gd.getLife(player1.getId());
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("A Dragon cast with mana from another source does not gain life")
    void dragonCastWithOtherManaDoesNotGainLife() {
        addReadyNurturer();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(createCreature("Test Dragon", CardSubtype.DRAGON)));

        int lifeBefore = gd.getLife(player1.getId());
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("The delayed life gain still triggers after the mana source leaves")
    void dragonCastAfterNurturerLeavesStillGainsLife() {
        Permanent nurturer = addReadyNurturer();
        harness.activateAbility(player1, 0, null, null);
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToHand(gd, nurturer));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new ScaledNurturer()));

        int lifeBefore = gd.getLife(player1.getId());
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("Each mana from separate activations triggers life gain")
    void twoManaFromSameNurturerGainFourLife() {
        Permanent nurturer = addReadyNurturer();
        harness.activateAbility(player1, 0, null, null);
        nurturer.untap();
        harness.activateAbility(player1, 0, null, null);
        harness.setHand(player1, List.of(new ScaledNurturer()));

        int lifeBefore = gd.getLife(player1.getId());
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 4);
    }

    private Permanent addReadyNurturer() {
        return addCreatureReady(player1, new ScaledNurturer());
    }

    private ManaPool manaPool() {
        return gd.playerManaPools.get(player1.getId());
    }

    private static Card createCreature(String name, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{G}");
        card.setColor(CardColor.GREEN);
        card.setPower(1);
        card.setToughness(1);
        card.setSubtypes(List.of(subtype));
        return card;
    }
}
