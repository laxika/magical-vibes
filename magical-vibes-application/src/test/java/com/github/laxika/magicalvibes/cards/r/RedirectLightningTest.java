package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RedirectLightning.class, Boomerang.class, GrizzlyBears.class, CounselOfTheSoratami.class})
class RedirectLightningTest extends BaseCardTest {

    @Test
    void redirectsSpellAfterPayingLife() {
        UUID target1 = addTargetCreatures();
        UUID target2 = harness.getPermanentId(player2, "Grizzly Bears");
        Boomerang boomerang = castBoomerang(target1);
        harness.setHand(player2, List.of(new RedirectLightning()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstantWithLifeOrManaAdditionalCost(player2, 0, boomerang.getId(), true);
        GameData gameData = harness.getGameData();
        assertThat(gameData.getLife(player2.getId())).isEqualTo(15);
        assertThat(gameData.playerManaPools.get(player2.getId()).getTotalAllMana()).isZero();

        harness.passBothPriorities();
        assertThat(gameData.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player2, target2);
        harness.passBothPriorities();

        assertThat(gameData.playerBattlefields.get(player1.getId())).extracting(p -> p.getId()).contains(target1);
        assertThat(gameData.playerBattlefields.get(player2.getId())).extracting(p -> p.getId()).doesNotContain(target2);
    }

    @Test
    void redirectsSpellAfterPayingMana() {
        UUID target1 = addTargetCreatures();
        UUID target2 = harness.getPermanentId(player2, "Grizzly Bears");
        Boomerang boomerang = castBoomerang(target1);
        harness.setHand(player2, List.of(new RedirectLightning()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castInstantWithLifeOrManaAdditionalCost(player2, 0, boomerang.getId(), false);
        GameData gameData = harness.getGameData();
        assertThat(gameData.getLife(player2.getId())).isEqualTo(20);
        assertThat(gameData.playerManaPools.get(player2.getId()).getTotalAllMana()).isZero();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, target2);
        harness.passBothPriorities();

        assertThat(gameData.playerBattlefields.get(player1.getId())).extracting(p -> p.getId()).contains(target1);
        assertThat(gameData.playerBattlefields.get(player2.getId())).extracting(p -> p.getId()).doesNotContain(target2);
    }

    @Test
    void requiresSingleTargetSpell() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new RedirectLightning()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstantWithLifeOrManaAdditionalCost(
                player2, 0, counsel.getId(), true))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("single target");
    }

    private UUID addTargetCreatures() {
        GrizzlyBears bears1 = new GrizzlyBears();
        GrizzlyBears bears2 = new GrizzlyBears();
        harness.addToBattlefield(player1, bears1);
        harness.addToBattlefield(player2, bears2);
        return harness.getPermanentId(player1, "Grizzly Bears");
    }

    private Boomerang castBoomerang(UUID targetId) {
        Boomerang boomerang = new Boomerang();
        harness.setHand(player1, List.of(boomerang));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, targetId);
        harness.passPriority(player1);
        return boomerang;
    }
}
