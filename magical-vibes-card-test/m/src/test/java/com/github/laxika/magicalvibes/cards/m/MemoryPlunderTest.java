package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RiteOfConsumption;
import com.github.laxika.magicalvibes.cards.s.SafeholdSentry;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MemoryPlunder.class, Shock.class, CounselOfTheSoratami.class, GrizzlyBears.class,
        Cancel.class, RiteOfConsumption.class, SafeholdSentry.class})
class MemoryPlunderTest extends BaseCardTest {

    @Test
    @DisplayName("Casts targeted instant from opponent's graveyard for free and prompts for its target")
    void castsTargetedInstantFromOpponentGraveyard() {
        Shock shock = new Shock();
        harness.setGraveyard(player2, List.of(shock));
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new MemoryPlunder()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveInstant(player1, 0, shock.getId());

        harness.handleMayAbilityChosen(player1, true);

        // Shock needs a target
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities(); // resolve Shock → 2 damage to Grizzly Bears

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Shock");
        harness.assertNotInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("Casts non-targeted sorcery from opponent's graveyard without paying its mana cost")
    void castsNonTargetedSorceryFromOpponentGraveyard() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setGraveyard(player2, List.of(counsel));

        harness.setHand(player1, List.of(new MemoryPlunder()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveInstant(player1, 0, counsel.getId());

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities(); // resolve Counsel of the Soratami → draw 2

        assertThat(gd.playerHands.get(player1.getId())).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("Declining the may-cast leaves the card in the opponent's graveyard")
    void decliningLeavesCardInGraveyard() {
        Shock shock = new Shock();
        harness.setGraveyard(player2, List.of(shock));

        harness.setHand(player1, List.of(new MemoryPlunder()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveInstant(player1, 0, shock.getId());

        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("Cannot target an instant in the caster's own graveyard")
    void cannotTargetOwnGraveyard() {
        Card shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));

        harness.setHand(player1, List.of(new MemoryPlunder()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, shock.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature card in the opponent's graveyard")
    void cannotTargetCreatureCard() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));

        harness.setHand(player1, List.of(new MemoryPlunder()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void targetLeavingGraveyardBeforeResolutionDoesNotOfferCast() {
        Shock shock = new Shock();
        harness.setGraveyard(player2, List.of(shock));
        harness.setHand(player1, List.of(new MemoryPlunder()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castInstant(player1, 0, shock.getId());
        harness.setGraveyard(player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Memory Plunder");
    }

    @Test
    void canCastCounterspellTargetingSpellStillOnStack() {
        Cancel cancel = new Cancel();
        Shock shock = new Shock();
        harness.setGraveyard(player2, List.of(cancel));
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.setHand(player1, List.of(new MemoryPlunder()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castAndResolveInstant(player1, 0, cancel.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, shock.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player2, "Shock");
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    void freeCastStillRequiresSacrificingCreatureAsAdditionalCost() {
        RiteOfConsumption rite = new RiteOfConsumption();
        harness.setGraveyard(player2, List.of(rite));
        UUID creatureId = harness.addToBattlefieldAndReturn(player1, new SafeholdSentry()).getId();
        harness.setHand(player1, List.of(new MemoryPlunder()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castAndResolveInstant(player1, 0, rite.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, creatureId);
        harness.assertNotOnBattlefield(player1, "Safehold Sentry");
        harness.assertInGraveyard(player1, "Safehold Sentry");
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
        harness.assertLife(player1, 22);
        harness.assertInGraveyard(player2, "Rite of Consumption");
    }
}
