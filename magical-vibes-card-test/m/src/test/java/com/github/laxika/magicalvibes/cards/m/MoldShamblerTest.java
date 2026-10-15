package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MoldShambler.class, AngelicChorus.class, GrizzlyBears.class, Forest.class})
class MoldShamblerTest extends BaseCardTest {

    @Test
    void entersWithoutKickerAndDoesNotDestroyPermanent() {
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.setHand(player1, List.of(new MoldShambler()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mold Shambler");
        harness.assertOnBattlefield(player2, "Angelic Chorus");
    }

    @Test
    void destroysTargetNoncreaturePermanentWhenKicked() {
        harness.addToBattlefield(player2, new AngelicChorus());
        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");
        harness.setHand(player1, List.of(new MoldShambler()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castKickedCreature(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mold Shambler");
        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
    }

    @Test
    void cannotTargetCreatureWhenKicked() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new MoldShambler()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, targetId))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Forest"));
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    void canBeKickedWithoutAnyLegalTarget() {
        harness.setHand(player1, List.of(new MoldShambler()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mold Shambler");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void choosesDestructionTargetAfterEnteringAndCanDestroyOwnLand() {
        harness.addToBattlefield(player1, new Forest());
        UUID targetId = harness.getPermanentId(player1, "Forest");
        harness.setHand(player1, List.of(new MoldShambler()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mold Shambler");
        harness.assertOnBattlefield(player1, "Forest");
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player1, "Mold Shambler");
    }
}
