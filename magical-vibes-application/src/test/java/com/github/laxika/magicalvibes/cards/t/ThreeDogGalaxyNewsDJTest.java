package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThreeDogGalaxyNewsDJ.class, GrizzlyBears.class, HolyStrength.class})
class ThreeDogGalaxyNewsDJTest extends BaseCardTest {

    @Test
    @DisplayName("Pays and sacrifices an Aura to copy it onto each other attacking creature")
    void copiesSacrificedAuraOntoOtherAttackingCreatures() {
        Permanent dog = addCreatureReady(player1, new ThreeDogGalaxyNewsDJ());
        Permanent firstAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        aura.setAttachedTo(dog.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, aura.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .contains("Holy Strength");
        List<Permanent> copiedAuras = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Holy Strength"))
                .toList();
        assertThat(copiedAuras).hasSize(2);
        assertThat(copiedAuras).extracting(Permanent::getAttachedTo)
                .containsExactlyInAnyOrder(firstAttacker.getId(), secondAttacker.getId());
    }

    @Test
    @DisplayName("Declining leaves the Aura and attackers unchanged")
    void decliningDoesNothing() {
        Permanent dog = addCreatureReady(player1, new ThreeDogGalaxyNewsDJ());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        aura.setAttachedTo(dog.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aura);
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId).doesNotContain(aura.getId());
    }
}
