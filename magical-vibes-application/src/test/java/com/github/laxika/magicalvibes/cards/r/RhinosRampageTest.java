package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.MirriCatWarrior;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RhinosRampage.class, MirriCatWarrior.class, HillGiant.class, RagingGoblin.class, Millstone.class, Ornithopter.class})
class RhinosRampageTest extends BaseCardTest {

    @Test
    void boostsYourCreatureBeforeItFights() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new MirriCatWarrior());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        cast(ownCreature, opposingCreature);

        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    void excessDamageTriggersOptionalArtifactDestruction() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Millstone());
        cast(ownCreature, opposingCreature);

        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Millstone");
    }

    @Test
    void excessDamageTriggerCannotTargetAnArtifactCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        cast(ownCreature, opposingCreature);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, artifactCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(Permanent ownCreature, Permanent opposingCreature) {
        harness.setHand(player1, List.of(new RhinosRampage()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castSorcery(player1, 0, List.of(ownCreature.getId(), opposingCreature.getId()));
        harness.passBothPriorities();
    }
}
