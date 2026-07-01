package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.EnteringCreatureMinPowerConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class EnterTriggerScanServiceTest {

    @Mock private GameQueryService gameQueryService;
    @Mock private GameBroadcastService gameBroadcastService;

    private EnterTriggerScanService service;
    private GameData gd;
    private UUID player1Id;

    @BeforeEach
    void setUp() {
        service = new EnterTriggerScanService(gameQueryService, gameBroadcastService);

        player1Id = UUID.randomUUID();
        gd = new GameData(UUID.randomUUID(), "test", player1Id, "Player1");
        gd.orderedPlayerIds.add(player1Id);
        gd.playerBattlefields.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
    }

    @Test
    @DisplayName("checkAllyCreatureEntersTriggers skips when entering card has null toughness (non-creature)")
    void checkAllyCreatureEntersTriggers_skipsWhenEnteringCardHasNullToughness() {
        Card entering = new Card();
        entering.setName("Forest");
        entering.setType(CardType.LAND);
        // toughness is null by default for lands

        service.checkAllyCreatureEntersTriggers(gd, player1Id, entering, 0);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("checkAllyCreatureEntersTriggers skips trigger when entering creature power is below min threshold")
    void checkAllyCreatureEntersTriggers_skipsTriggerWhenPowerBelowMinThreshold() {
        // Set up a triggering permanent on the battlefield
        Card sourcePermanent = new Card();
        sourcePermanent.setName("Garruk's Packleader");
        GainLifeEffect innerEffect = new GainLifeEffect(1);
        sourcePermanent.addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new EnteringCreatureMinPowerConditionalEffect(3, innerEffect));
        gd.playerBattlefields.get(player1Id).add(new Permanent(sourcePermanent));

        Card entering = new Card();
        entering.setName("Grizzly Bears");
        entering.setType(CardType.CREATURE);
        entering.setPower(2);
        entering.setToughness(2);

        service.checkAllyCreatureEntersTriggers(gd, player1Id, entering, 0);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("checkAllyCreatureEntersTriggers pushes trigger onto stack when entering creature power meets min threshold")
    void checkAllyCreatureEntersTriggers_pushesToStackWhenPowerMeetsMinThreshold() {
        Card sourcePermanent = new Card();
        sourcePermanent.setName("Garruk's Packleader");
        GainLifeEffect innerEffect = new GainLifeEffect(1);
        sourcePermanent.addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new EnteringCreatureMinPowerConditionalEffect(3, innerEffect));
        gd.playerBattlefields.get(player1Id).add(new Permanent(sourcePermanent));

        Card entering = new Card();
        entering.setName("Kalonian Tusker");
        entering.setType(CardType.CREATURE);
        entering.setPower(4);
        entering.setToughness(4);

        service.checkAllyCreatureEntersTriggers(gd, player1Id, entering, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(sourcePermanent);
    }

    @Test
    @DisplayName("checkAllyCreatureEntersTriggers does not count the entering creature itself as a trigger source")
    void checkAllyCreatureEntersTriggers_doesNotTriggerSelf() {
        // The entering creature itself also has the ally-creature trigger (e.g. two copies entering)
        Card entering = new Card();
        entering.setName("Self-Triggering Creature");
        entering.setType(CardType.CREATURE);
        entering.setPower(4);
        entering.setToughness(4);
        entering.addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new EnteringCreatureMinPowerConditionalEffect(3, new GainLifeEffect(1)));
        // The entering card's own permanent is on the battlefield
        gd.playerBattlefields.get(player1Id).add(new Permanent(entering));

        service.checkAllyCreatureEntersTriggers(gd, player1Id, entering, 0);

        // The `if (perm.getCard() == enteringCreature) continue;` guard skips self — no stack entry
        assertThat(gd.stack).isEmpty();
    }
}
