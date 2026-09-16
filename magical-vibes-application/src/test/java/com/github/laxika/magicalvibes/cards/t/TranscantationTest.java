package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Transcantation.class, Unsummon.class, GrizzlyBears.class})
class TranscantationTest extends BaseCardTest {

    @Test
    void turnsTargetSpellIntoLightningBoltAndCanKeepItsTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Unsummon unsummon = new Unsummon();

        harness.setHand(player1, List.of(unsummon));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player2, List.of(new Transcantation()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, unsummon.getId());
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        StackEntry transformed = gameData.stack.getFirst();
        assertThat(transformed.getCard().getName()).isEqualTo("Lightning Bolt");
        assertThat(transformed.getEffectsToResolve()).hasSize(1);

        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gameData.playerBattlefields.get(player1.getId())).doesNotContain(target);
    }

    @Test
    void canRetargetTheTransformedSpell() {
        Permanent originalTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent newTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Unsummon unsummon = new Unsummon();

        harness.setHand(player1, List.of(unsummon));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player2, List.of(new Transcantation()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player1, 0, originalTarget.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, unsummon.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, newTarget.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(originalTarget)
                .doesNotContain(newTarget);
    }

    @Test
    void cannotTargetCreatureSpell() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new Transcantation()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0,
                harness.getGameData().stack.getFirst().getCard().getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
