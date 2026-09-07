package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Flickerform.class, GrizzlyBears.class, HolyStrength.class})
class FlickerformTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the enchanted creature and all attached Auras, then returns them attached at the next end step")
    void exilesAndReturnsEnchantedCreatureAndAuras() {
        Permanent creature = setupFlickerform();

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"))
                .anyMatch(card -> card.getName().equals("Flickerform"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Holy Strength"));

        advanceToEndStep();

        Permanent returnedCreature = findPermanent(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Flickerform")
                        && returnedCreature.getId().equals(permanent.getAttachedTo()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Holy Strength")
                        && returnedCreature.getId().equals(permanent.getAttachedTo()));
        assertThat(gd.getPlayerExiledCards(player1.getId())).noneMatch(card -> card.getName().equals("Flickerform"));
        assertThat(gd.getPlayerExiledCards(player2.getId())).noneMatch(card -> card.getName().equals("Holy Strength"));
        assertThat(creature.getId()).isNotEqualTo(returnedCreature.getId());
    }

    @Test
    @DisplayName("Leaves the other exiled cards in exile if the enchanted creature does not return")
    void doesNotReturnAurasWithoutTheCreature() {
        Permanent creature = setupFlickerform();

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        gd.addCardToHand(player1.getId(), creature.getCard());
        gd.removeFromExile(creature.getCard().getId());

        advanceToEndStep();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Flickerform"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Holy Strength"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Flickerform"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Holy Strength"));
    }

    private Permanent setupFlickerform() {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(creature);

        Permanent flickerform = new Permanent(new Flickerform());
        flickerform.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(flickerform);

        HolyStrength holyStrength = new HolyStrength();
        holyStrength.setOwnerId(player2.getId());
        Permanent otherAura = new Permanent(holyStrength);
        otherAura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player2.getId()).add(otherAura);
        return creature;
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
