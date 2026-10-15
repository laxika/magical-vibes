package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.q.QuintoriusKand;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({GetLost.class, GrizzlyBears.class, Forest.class, GloriousAnthem.class, QuintoriusKand.class})
class GetLostTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a creature and creates two Map tokens")
    void destroysCreatureAndCreatesMaps() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(target);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(findPermanents(player1, "Map")).isEmpty();
        assertThat(findPermanents(player2, "Map")).hasSize(2);
        assertThat(findPermanents(player2, "Map")).allMatch(map ->
                map.getCard().hasType(CardType.ARTIFACT)
                        && map.getCard().getSubtypes().contains(CardSubtype.MAP));
    }

    @Test
    @DisplayName("Can destroy an enchantment")
    void destroysEnchantment() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());

        cast(target);

        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        assertThat(findPermanents(player1, "Map")).isEmpty();
        assertThat(findPermanents(player2, "Map")).hasSize(2);
    }

    @Test
    @DisplayName("Cannot target a land")
    void rejectsLandTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new GetLost()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A Map token sacrifices itself to make a creature explore at sorcery speed")
    void mapExploresCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent toDestroy = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));

        cast(toDestroy);
        Permanent map = findPermanents(player1, "Map").getFirst();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, battlefieldIndex(player1, map), 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).contains(land.getId());
        assertThat(findPermanents(player1, "Map")).hasSize(1);
    }

    @Test
    @DisplayName("A Map token cannot target an opponent's creature")
    void mapRequiresCreatureYouControl() {
        Permanent map = addMap();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(player1, map), 0,
                null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Destroying your own creature gives you two Maps")
    void destroysOwnCreatureAndCreatesMaps() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        cast(target);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(findPermanents(player1, "Map")).hasSize(2);
        assertThat(findPermanents(player2, "Map")).isEmpty();
    }

    @Test
    @DisplayName("Destroys a planeswalker and gives its controller two Maps")
    void destroysPlaneswalkerAndCreatesMapsForItsController() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new QuintoriusKand());
        target.setCounterCount(CounterType.LOYALTY, 4);

        cast(target);

        harness.assertInGraveyard(player2, "Quintorius Kand");
        assertThat(findPermanents(player2, "Map")).hasSize(2);
        assertThat(findPermanents(player1, "Map")).isEmpty();
    }

    @Test
    @DisplayName("Indestructibility does not prevent the target's controller from creating Maps")
    void indestructibleTargetStillGivesItsControllerMaps() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setCounterCount(CounterType.INDESTRUCTIBLE, 1);

        cast(target);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(findPermanents(player2, "Map")).hasSize(2);
        assertThat(findPermanents(player1, "Map")).isEmpty();
    }

    @Test
    @DisplayName("Get Lost does not resolve or create Maps when its target is absent")
    void absentTargetCreatesNoMaps() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GetLost()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Map")).isEmpty();
        assertThat(findPermanents(player2, "Map")).isEmpty();
        harness.assertInGraveyard(player1, "Get Lost");
    }

    @Test
    @DisplayName("A Map cannot be activated outside a main phase")
    void mapRequiresSorceryTiming() {
        Permanent map = addMap();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(player1, map), 0,
                null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanents(player1, "Map")).hasSize(2);
    }

    @Test
    @DisplayName("A Map exploring an empty library puts a counter on its target")
    void mapExploresEmptyLibrary() {
        Permanent map = addMap();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(player1, map), 0, null, target.getId());
        assertThat(findPermanents(player1, "Map")).hasSize(1);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Get Lost cannot target a Map artifact")
    void rejectsArtifactOnlyTarget() {
        Permanent map = addMap();
        harness.setHand(player1, List.of(new GetLost()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, map.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A Map puts a counter on its creature and may put a revealed nonland into the graveyard")
    void mapExploresNonland() {
        Permanent map = addMap();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card revealed = new GetLost();
        harness.setLibrary(player1, List.of(revealed));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(player1, map), 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId).contains(revealed.getId());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(findPermanents(player1, "Map")).hasSize(1);
    }

    private void cast(Permanent target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GetLost()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private Permanent addMap() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        cast(target);
        return findPermanents(player1, "Map").getFirst();
    }

    private int battlefieldIndex(com.github.laxika.magicalvibes.model.Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
