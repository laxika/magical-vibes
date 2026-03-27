package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BeaconOfUnrest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.ThrashOfRaptors;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.CastFromHandConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WakeningSunsAvatarTest extends BaseCardTest {

    @Test
    @DisplayName("Wakening Sun's Avatar has correct ETB effect wrapped in cast-from-hand conditional")
    void hasCorrectProperties() {
        WakeningSunsAvatar card = new WakeningSunsAvatar();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(CastFromHandConditionalEffect.class);
        CastFromHandConditionalEffect conditional = (CastFromHandConditionalEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(conditional.wrapped()).isInstanceOf(DestroyAllPermanentsEffect.class);
    }

    @Test
    @DisplayName("When cast from hand, all non-Dinosaur creatures are destroyed")
    void castFromHandDestroysNonDinosaurs() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new WakeningSunsAvatar()));
        harness.addMana(player1, ManaColor.WHITE, 8);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("When cast from hand, Dinosaur creatures survive")
    void castFromHandSparesDinosaurs() {
        harness.addToBattlefield(player1, new ThrashOfRaptors());
        harness.addToBattlefield(player2, new ThrashOfRaptors());

        harness.setHand(player1, List.of(new WakeningSunsAvatar()));
        harness.addMana(player1, ManaColor.WHITE, 8);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Thrash of Raptors"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Thrash of Raptors"));
    }

    @Test
    @DisplayName("Wakening Sun's Avatar itself survives its own ETB since it is a Dinosaur")
    void avatarItselfsurvivestBecauseItIsADinosaur() {
        harness.setHand(player1, List.of(new WakeningSunsAvatar()));
        harness.addMana(player1, ManaColor.WHITE, 8);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Wakening Sun's Avatar"));
    }

    @Test
    @DisplayName("When entering not from hand, non-Dinosaur creatures are not destroyed")
    void enteringNotFromHandDoesNotDestroyCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setGraveyard(player1, List.of(new WakeningSunsAvatar()));
        harness.setHand(player1, List.of(new BeaconOfUnrest()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.GRAVEYARD_CHOICE);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }
}
