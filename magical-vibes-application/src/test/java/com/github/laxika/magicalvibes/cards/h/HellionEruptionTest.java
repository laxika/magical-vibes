package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.k.KozileksPredator;
import com.github.laxika.magicalvibes.cards.n.NestInvader;
import com.github.laxika.magicalvibes.cards.p.PawnOfUlamog;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HellionEruption.class, Forest.class, NestInvader.class, KozileksPredator.class, PawnOfUlamog.class})
class HellionEruptionTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices your creatures and creates one 4/4 Hellion for each")
    void sacrificesCreaturesAndCreatesMatchingHellions() {
        harness.setHand(player1, List.of(new HellionEruption()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.addToBattlefield(player1, new NestInvader());
        harness.addToBattlefield(player1, new KozileksPredator());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new NestInvader());

        harness.castAndResolveSorcery(player1, 0, 0);

        List<Permanent> hellions = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(hellions).hasSize(2);
        assertThat(hellions).allSatisfy(hellion -> {
            assertThat(hellion.getEffectivePower()).isEqualTo(4);
            assertThat(hellion.getEffectiveToughness()).isEqualTo(4);
            assertThat(hellion.getCard().getColor()).isEqualTo(CardColor.RED);
            assertThat(hellion.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(hellion.getCard().getSubtypes()).containsExactly(CardSubtype.HELLION);
        });
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Nest Invader");
        harness.assertInGraveyard(player1, "Nest Invader");
        harness.assertInGraveyard(player1, "Kozilek's Predator");
    }

    @Test
    @DisplayName("Creates no Hellions when you control no creatures")
    void createsNoHellionsWithoutCreatures() {
        harness.setHand(player1, List.of(new HellionEruption()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Sacrificed creature tokens count toward the number of Hellions")
    void countsSacrificedTokens() {
        harness.enterBattlefieldAndReturn(player1, new NestInvader());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Eldrazi Spawn");
        harness.setHand(player1, List.of(new HellionEruption()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2)
                .allSatisfy(permanent -> {
                    assertThat(permanent.getCard().isToken()).isTrue();
                    assertThat(permanent.getCard().getName()).isEqualTo("Hellion");
                });
        harness.assertInGraveyard(player1, "Nest Invader");
        harness.assertNotInGraveyard(player1, "Eldrazi Spawn");
    }

    @Test
    @DisplayName("Counts creatures present at resolution rather than at casting")
    void countsCreaturesAtResolution() {
        harness.setHand(player1, List.of(new HellionEruption()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.castSorcery(player1, 0, 0);
        harness.addToBattlefield(player1, new NestInvader());

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1)
                .allSatisfy(permanent -> assertThat(permanent.getCard().getName()).isEqualTo("Hellion"));
        harness.assertInGraveyard(player1, "Nest Invader");
    }

    @Test
    @DisplayName("Pawn of Ulamog sees every nontoken creature sacrificed simultaneously")
    void sacrificesAllCreaturesSimultaneously() {
        harness.addToBattlefield(player1, new PawnOfUlamog());
        harness.addToBattlefield(player1, new NestInvader());
        harness.setHand(player1, List.of(new HellionEruption()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2)
                .allSatisfy(permanent -> assertThat(permanent.getCard().getName()).isEqualTo("Hellion"));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Eldrazi Spawn"))
                .hasSize(2);
        harness.assertInGraveyard(player1, "Pawn of Ulamog");
        harness.assertInGraveyard(player1, "Nest Invader");
    }
}
