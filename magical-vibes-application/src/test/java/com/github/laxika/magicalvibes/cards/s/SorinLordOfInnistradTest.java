package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LilianaOfTheVeil;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyUpToTargetsThenReturnFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SorinLordOfInnistradEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SorinLordOfInnistradTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has three loyalty abilities")
    void hasThreeAbilities() {
        SorinLordOfInnistrad card = new SorinLordOfInnistrad();
        assertThat(card.getActivatedAbilities()).hasSize(3);
    }

    

    

    

    // ===== Casting =====

    @Test
    @DisplayName("Resolving puts planeswalker on battlefield with 3 loyalty")
    void resolvingEntersBattlefieldWithLoyalty() {
        harness.setHand(player1, List.of(new SorinLordOfInnistrad()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        Permanent sorin = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Sorin, Lord of Innistrad"))
                .findFirst().orElseThrow();
        assertThat(sorin.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    // ===== +1 ability =====

    @Test
    @DisplayName("+1 creates a 1/1 black Vampire token with lifelink")
    void plusOneCreatesVampireToken() {
        Permanent sorin = addReadySorin(player1);
        sorin.setCounterCount(CounterType.LOYALTY, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(sorin.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .findFirst().orElseThrow();
        assertThat(token.getCard().getName()).isEqualTo("Vampire");
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.VAMPIRE);
        assertThat(token.getCard().getKeywords()).contains(Keyword.LIFELINK);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
    }

    // ===== -2 ability =====

    @Test
    @DisplayName("-2 creates an emblem with +1/+0 for creatures you control")
    void minusTwoCreatesEmblem() {
        Permanent sorin = addReadySorin(player1);
        sorin.setCounterCount(CounterType.LOYALTY, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(sorin.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.emblems).hasSize(1);

        Emblem emblem = gd.emblems.getFirst();
        assertThat(emblem.controllerId()).isEqualTo(player1.getId());
        assertThat(emblem.staticEffects()).hasSize(1);
        assertThat(emblem.staticEffects().getFirst()).isEqualTo(new StaticBoostEffect(1, 0, GrantScope.OWN_CREATURES));
    }

    @Test
    @DisplayName("Emblem gives creatures you control +1/+0")
    void emblemBoostsControlledCreatures() {
        Permanent sorin = addReadySorin(player1);
        sorin.setCounterCount(CounterType.LOYALTY, 3);
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    // ===== -6 ability =====

    @Test
    @DisplayName("-6 destroys up to three creatures and returns them under your control")
    void minusSixDestroysAndReturnsCreatures() {
        Permanent sorin = addReadySorin(player1);
        sorin.setCounterCount(CounterType.LOYALTY, 7);

        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        List<UUID> targetIds = harness.getGameData().playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .map(Permanent::getId)
                .toList();

        harness.activateAbilityWithMultiTargets(player1, 0, 2, targetIds);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(sorin.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .count()).isEqualTo(3);
    }

    @Test
    @DisplayName("-6 can target fewer than three permanents")
    void minusSixCanTargetFewerThanThree() {
        Permanent sorin = addReadySorin(player1);
        sorin.setCounterCount(CounterType.LOYALTY, 7);
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID bearsId = harness.getGameData().playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow().getId();

        harness.activateAbilityWithMultiTargets(player1, 0, 2, List.of(bearsId));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("-6 can be activated with zero targets")
    void minusSixCanActivateWithZeroTargets() {
        Permanent sorin = addReadySorin(player1);
        sorin.setCounterCount(CounterType.LOYALTY, 7);

        harness.activateAbilityWithMultiTargets(player1, 0, 2, List.of());
        harness.passBothPriorities();

        assertThat(sorin.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("-6 returns destroyed planeswalkers under your control")
    void minusSixReturnsDestroyedPlaneswalker() {
        Permanent sorin = addReadySorin(player1);
        sorin.setCounterCount(CounterType.LOYALTY, 7);
        harness.addToBattlefield(player2, new LilianaOfTheVeil());
        Permanent liliana = harness.getGameData().playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Liliana of the Veil"))
                .findFirst().orElseThrow();
        liliana.setCounterCount(CounterType.LOYALTY, 3);

        UUID lilianaId = liliana.getId();

        harness.activateAbilityWithMultiTargets(player1, 0, 2, List.of(lilianaId));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Liliana of the Veil"));
        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Liliana of the Veil"))
                .findFirst().orElseThrow();
        assertThat(returned.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("-6 does not return indestructible permanents")
    void minusSixDoesNotReturnIndestructiblePermanent() {
        Permanent sorin = addReadySorin(player1);
        sorin.setCounterCount(CounterType.LOYALTY, 7);
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID bearsId = harness.getGameData().playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow().getId();
        Permanent bears = harness.getGameData().playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getId().equals(bearsId))
                .findFirst().orElseThrow();
        bears.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);

        harness.activateAbilityWithMultiTargets(player1, 0, 2, List.of(bearsId));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot target Sorin himself with -6")
    void cannotTargetSelfWithMinusSix() {
        Permanent sorin = addReadySorin(player1);
        sorin.setCounterCount(CounterType.LOYALTY, 7);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 2, List.of(sorin.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another planeswalker");
    }

    @Test
    @DisplayName("Cannot activate -6 with insufficient loyalty")
    void cannotActivateMinusSixWithInsufficientLoyalty() {
        addReadySorin(player1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough loyalty");
    }

    // ===== Helpers =====

    private Permanent addReadySorin(Player player) {
        SorinLordOfInnistrad card = new SorinLordOfInnistrad();
        Permanent perm = new Permanent(card);
        perm.setCounterCount(CounterType.LOYALTY, 3);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
