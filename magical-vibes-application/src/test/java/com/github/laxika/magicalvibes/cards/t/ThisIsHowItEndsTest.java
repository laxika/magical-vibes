package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetCreatureThenOwnerFacesVillainousChoiceEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThisIsHowItEnds.class, GrizzlyBears.class, Forest.class})
class ThisIsHowItEndsTest extends BaseCardTest {

    @Test
    void shufflesTargetThenOwnerCanChooseLifeLoss() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player2, List.of(new Forest()));
        castAt(target);

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly(ShuffleTargetCreatureThenOwnerFacesVillainousChoiceEffect.LOSE_LIFE_OPTION,
                        ShuffleTargetCreatureThenOwnerFacesVillainousChoiceEffect.SHUFFLE_CREATURE_OPTION);

        harness.handleListChoice(player2,
                ShuffleTargetCreatureThenOwnerFacesVillainousChoiceEffect.LOSE_LIFE_OPTION);

        harness.assertLife(player2, 15);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }

    @Test
    void ownerCanShuffleAnotherOwnedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent firstOther = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondOther = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player2, List.of(new Forest()));
        castAt(target);

        harness.handleListChoice(player2,
                ShuffleTargetCreatureThenOwnerFacesVillainousChoiceEffect.SHUFFLE_CREATURE_OPTION);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(firstOther.getId(), secondOther.getId());
        harness.handlePermanentChosen(player2, firstOther.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(p -> p.getCard().getName())
                .containsExactly("Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId())
                        || permanent.getId().equals(firstOther.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(secondOther.getId()));
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new ThisIsHowItEnds()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castAt(Permanent target) {
        harness.setHand(player1, List.of(new ThisIsHowItEnds()));
        addMana();
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
