package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MirriCatWarrior;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EmberIslandProduction.class, GrizzlyBears.class, MirriCatWarrior.class})
class EmberIslandProductionTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a nonlegendary 4/4 Hero copy of a creature you control")
    void createsHeroCopyOfOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MirriCatWarrior());
        cast(0, target, player1);

        Permanent token = tokenCopy(player1);
        assertThat(token.getCard().getPower()).isEqualTo(4);
        assertThat(token.getCard().getToughness()).isEqualTo(4);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.CAT, CardSubtype.HERO);
        assertThat(token.getCard().getSupertypes()).doesNotContain(CardSupertype.LEGENDARY);
    }

    @Test
    @DisplayName("Creates a nonlegendary 2/2 Coward copy of an opponent's creature")
    void createsCowardCopyOfOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MirriCatWarrior());
        cast(1, target, player2);

        Permanent token = tokenCopy(player1);
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.CAT, CardSubtype.COWARD);
        assertThat(token.getCard().getSupertypes()).doesNotContain(CardSupertype.LEGENDARY);
    }

    @Test
    @DisplayName("Each mode enforces its controller restriction")
    void modesEnforceControllerRestrictions() {
        Permanent ownTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new EmberIslandProduction()));
        addMana();
        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, 0, List.of(opposingTarget.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player1, List.of(new EmberIslandProduction()));
        addMana();
        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, 1, List.of(ownTarget.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int mode, Permanent target, com.github.laxika.magicalvibes.model.Player targetController) {
        harness.setHand(player1, List.of(new EmberIslandProduction()));
        addMana();
        harness.castModalSorcery(player1, 0, mode, List.of(target.getId()));
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private Permanent tokenCopy(com.github.laxika.magicalvibes.model.Player controller) {
        return gd.playerBattlefields.get(controller.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
    }
}
