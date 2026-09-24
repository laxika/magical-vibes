package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.b.BlazingSpecter;
import com.github.laxika.magicalvibes.cards.d.DreamThrush;
import com.github.laxika.magicalvibes.cards.g.GohamDjinn;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Wallop.class, DreamThrush.class, BlazingSpecter.class, AngelOfMercy.class, GohamDjinn.class})
class WallopTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a blue creature with flying")
    void destroysBlueCreatureWithFlying() {
        harness.addToBattlefield(player2, new DreamThrush());
        harness.setHand(player1, List.of(new Wallop()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, harness.getPermanentId(player2, "Dream Thrush"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Dream Thrush");
        harness.assertInGraveyard(player2, "Dream Thrush");
    }

    @Test
    @DisplayName("Destroys a black creature with flying")
    void destroysBlackCreatureWithFlying() {
        harness.addToBattlefield(player2, new BlazingSpecter());
        harness.setHand(player1, List.of(new Wallop()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, harness.getPermanentId(player2, "Blazing Specter"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Blazing Specter");
        harness.assertInGraveyard(player2, "Blazing Specter");
    }

    @Test
    @DisplayName("Cannot target a flying creature that is neither blue nor black")
    void cannotTargetOtherColorFlyingCreature() {
        harness.addToBattlefield(player1, new DreamThrush());
        Permanent angelOfMercy = harness.addToBattlefieldAndReturn(player2, new AngelOfMercy());
        harness.setHand(player1, List.of(new Wallop()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, angelOfMercy.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("blue or black creature with flying");
    }

    @Test
    @DisplayName("Cannot target a black creature without flying")
    void cannotTargetBlackCreatureWithoutFlying() {
        Permanent gohamDjinn = harness.addToBattlefieldAndReturn(player2, new GohamDjinn());
        harness.setHand(player1, List.of(new Wallop()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, gohamDjinn.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("blue or black creature with flying");
    }
}
