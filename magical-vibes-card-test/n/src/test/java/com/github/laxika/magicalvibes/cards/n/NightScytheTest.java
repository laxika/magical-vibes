package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NightScythe.class, GrizzlyBears.class})
class NightScytheTest extends BaseCardTest {

    @Test
    void entersAndCreatesAnArtifactNecronWarriorToken() {
        harness.castFromHand(player1, new NightScythe(), "{3}");
        resolveAllTriggers();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().getCard().getName()).isEqualTo("Necron Warrior");
        assertThat(gqs.getEffectivePower(gd, tokens.getFirst())).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, tokens.getFirst())).isEqualTo(2);
        assertThat(gqs.getEffectiveCardTypes(gd, tokens.getFirst())).contains(CardType.ARTIFACT);
    }

    @Test
    void crewsWithTwoPower() {
        Permanent scythe = addCreatureReady(player1, new NightScythe());
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(scythe), null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, scythe)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    void newlyCreatedTokenCanCrewBeforeItCanAttack() {
        harness.castFromHand(player1, new NightScythe(), "{3}");
        resolveAllTriggers();
        Permanent scythe = findPermanent(player1, "Night Scythe");
        Permanent token = findPermanent(player1, "Necron Warrior");

        assertThat(token.isSummoningSick()).isTrue();
        assertThat(gqs.getEffectiveColors(gd, token)).containsExactly(CardColor.BLACK);
        assertThat(gqs.effectiveCreatureSubtypes(gd, token))
                .contains(CardSubtype.NECRON, CardSubtype.WARRIOR);
        assertThat(gqs.isCreature(gd, scythe)).isFalse();

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(scythe), null, null);

        assertThat(token.isTapped()).isTrue();
        assertThat(scythe.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, scythe)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, scythe)).isTrue();
        assertThat(gqs.getEffectiveCardTypes(gd, scythe)).contains(CardType.ARTIFACT);
        assertThat(countPermanents(player1, "Necron Warrior")).isEqualTo(1);
    }

    @Test
    void enteringUnderOpponentsControlCreatesTokenForThatController() {
        harness.enterBattlefieldAndReturn(player2, new NightScythe());
        resolveAllTriggers();

        assertThat(countPermanents(player2, "Necron Warrior")).isEqualTo(1);
        assertThat(countPermanents(player1, "Necron Warrior")).isZero();
    }
}
