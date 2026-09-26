package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AvenEnvoy;
import com.github.laxika.magicalvibes.cards.c.ChromeshellCrab;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MasterOfTheVeil.class, ChromeshellCrab.class, AvenEnvoy.class})
class MasterOfTheVeilTest extends BaseCardTest {

    @Test
    void turningFaceUpTurnsTargetCreatureWithMorphFaceDown() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChromeshellCrab());
        harness.setHand(player1, List.of(new MasterOfTheVeil()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        Permanent master = findPermanent(player1, "Master of the Veil");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(master));

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.isFaceDown()).isTrue();
        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        assertThat(master.isFaceDown()).isFalse();
    }

    @Test
    void doesNotOfferCreatureWithoutMorphAsTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AvenEnvoy());
        harness.setHand(player1, List.of(new MasterOfTheVeil()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        Permanent master = findPermanent(player1, "Master of the Veil");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(master));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(master.getId())
                .doesNotContain(target.getId());
    }

    @Test
    void decliningFaceUpAbilityLeavesTargetFaceUp() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChromeshellCrab());
        harness.setHand(player1, List.of(new MasterOfTheVeil()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        Permanent master = findPermanent(player1, "Master of the Veil");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(master));

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.isFaceDown()).isFalse();
    }
}
