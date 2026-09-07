package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GenerousSoul;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BelovedBeggar.class, GenerousSoul.class})
class BelovedBeggarTest extends BaseCardTest {

    @Test
    @DisplayName("Disturb casts Beloved Beggar from the graveyard transformed as Generous Soul")
    void disturbEntersTransformed() {
        Permanent soul = castWithDisturb();

        assertThat(soul.isTransformed()).isTrue();
        assertThat(soul.getCard().getName()).isEqualTo("Generous Soul");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Generous Soul is exiled instead of going to the graveyard")
    void generousSoulExiledInsteadOfGraveyard() {
        Permanent soul = castWithDisturb();
        UUID soulId = soul.getOriginalCard().getId();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, soul));

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards.stream().map(exiled -> exiled.card().getId())).contains(soulId);
    }

    private Permanent castWithDisturb() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new BelovedBeggar()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }
}
