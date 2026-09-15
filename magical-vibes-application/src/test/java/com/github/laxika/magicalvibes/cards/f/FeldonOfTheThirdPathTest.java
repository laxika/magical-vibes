package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FeldonOfTheThirdPath.class, GrizzlyBears.class, Plains.class})
class FeldonOfTheThirdPathTest extends BaseCardTest {

    @Test
    @DisplayName("Creates an artifact creature copy with haste and sacrifices it at the next end step")
    void createsHastyArtifactCopyAndSacrificesItAtEndStep() {
        Permanent feldon = harness.addToBattlefieldAndReturn(player1, new FeldonOfTheThirdPath());
        feldon.setSummoningSick(false);
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        addFeldonMana();

        int feldonIndex = gd.playerBattlefields.get(player1.getId()).indexOf(feldon);
        harness.activateAbilityWithGraveyardTargets(player1, feldonIndex, 0, List.of(bears.getId()));
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Grizzly Bears"))
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(token.hasKeyword(Keyword.HASTE)).isTrue();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(token.getId()));
    }

    @Test
    @DisplayName("Rejects a noncreature graveyard target")
    void rejectsNoncreatureTarget() {
        Permanent feldon = harness.addToBattlefieldAndReturn(player1, new FeldonOfTheThirdPath());
        feldon.setSummoningSick(false);
        Card plains = new Plains();
        harness.setGraveyard(player1, List.of(plains));
        addFeldonMana();

        int feldonIndex = gd.playerBattlefields.get(player1.getId()).indexOf(feldon);
        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, feldonIndex, 0, List.of(plains.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature card");
    }

    private void addFeldonMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
