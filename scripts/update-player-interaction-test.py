#!/usr/bin/env python3
"""Update PlayerInteractionResolutionServiceTest to use handlers + support."""

import re
from pathlib import Path

TEST = Path("magical-vibes-backend/src/test/java/com/github/laxika/magicalvibes/service/effect/PlayerInteractionResolutionServiceTest.java")
src = TEST.read_text(encoding="utf-8")

NO_EFFECT_PARAM = {
    "resolveDrawXCardsForTargetPlayer": "DrawXCardsForTargetPlayerEffect",
    "resolveDrawXCards": "DrawXCardsEffect",
    "resolveShuffleHandIntoLibraryAndDraw": "ShuffleHandIntoLibraryAndDrawEffect",
    "resolveDrawCardsEqualToChargeCounters": "DrawCardsEqualToChargeCountersOnSourceEffect",
    "resolveDrawCardsEqualToControlledCreatureCount": "DrawCardsEqualToControlledCreatureCountEffect",
    "resolveDiscardCardUnlessAttackedThisTurn": "DiscardCardUnlessAttackedThisTurnEffect",
    "resolveDiscardOwnHand": "DiscardOwnHandEffect",
    "resolveDiscardOwnHandThenDrawThatMany": "DiscardOwnHandThenDrawThatManyEffect",
    "resolveRegisterDelayedCombatDamageLoot": "RegisterDelayedCombatDamageLootEffect",
    "resolveTargetPlayerDiscardsByChargeCounters": "TargetPlayerDiscardsByChargeCountersEffect",
    "resolveChooseCardNameAndExileFromZones": "ChooseCardNameAndExileFromZonesEffect",
    "resolveExileTargetGraveyardCardAndSameNameFromZones": "ExileTargetGraveyardCardAndSameNameFromZonesEffect",
    "resolveTargetPlayerRandomDiscardOrControllerDraws": "TargetPlayerRandomDiscardOrControllerDrawsEffect",
    "resolveTargetPlayerRandomDiscardX": "TargetPlayerRandomDiscardXEffect",
    "resolveReturnPermanentsOnCombatDamage": "ReturnPermanentsOnCombatDamageToPlayerEffect",
    "resolvePutAwakeningCounters": "PutAwakeningCountersOnTargetLandsEffect",
    "resolveLookAtHand": "LookAtHandEffect",
    "resolveRevealRandomCardFromTargetPlayerHand": "RevealRandomCardFromTargetPlayerHandEffect",
    "resolveChangeColorText": "ChangeColorTextEffect",
    "resolveRedirectDraws": "RedirectDrawsEffect",
    "resolveGrantPermanentNoMaxHandSize": "GrantPermanentNoMaxHandSizeEffect",
    "resolveRevealRandomHandCardAndPlay": "RevealRandomHandCardAndPlayEffect",
    "resolveOpponentMayPlayCreature": "OpponentMayPlayCreatureEffect",
}

src = src.replace(
    "import com.github.laxika.magicalvibes.service.effect.PlayerInteractionResolutionService;",
    "import com.github.laxika.magicalvibes.service.effect.EffectHandler;\n"
    "import com.github.laxika.magicalvibes.service.effect.normalfx.NormalEffectHandlerBean;\n"
    "import com.github.laxika.magicalvibes.service.effect.normalfx.NormalEffectHandlerBeanFactory;\n"
    "import com.github.laxika.magicalvibes.service.effect.normalfx.PlayerInteractionSupport;",
)

src = src.replace(
    "@Mock private EffectHandlerRegistry effectHandlerRegistry;\n\n    @InjectMocks private PlayerInteractionResolutionService service;",
    "private EffectHandlerRegistry registry;\n    private PlayerInteractionSupport support;",
)

src = src.replace("service.startNextEachPlayerDiscard(", "support.startNextEachPlayerDiscard(")

# resolveXxx(gd, entry, effectVar) -> resolveEffect(...)
src = re.sub(
    r"service\.resolve\w+\(gd, entry, (\w+)\);",
    r"resolveEffect(gd, entry, \1);",
    src,
)

for method, effect_class in NO_EFFECT_PARAM.items():
    src = src.replace(
        f"service.{method}(gd, entry);",
        f"resolveEffect(gd, entry, new {effect_class}());",
    )

setup = """
        support = new PlayerInteractionSupport(drawService, graveyardService, gameQueryService,
                gameBroadcastService, playerInputService, sessionManager, cardViewFactory,
                permanentRemovalService, battlefieldEntryService, triggerCollectionService);
        registry = new EffectHandlerRegistry();
        java.util.List<NormalEffectHandlerBean> beans = NormalEffectHandlerBeanFactory.createPlayerInteractionHandlers(
                support, drawService, graveyardService, gameQueryService, gameBroadcastService,
                playerInputService, sessionManager, cardViewFactory, permanentRemovalService,
                battlefieldEntryService, triggerCollectionService, registry);
        NormalEffectHandlerBeanFactory.registerAll(beans, registry);
"""

helper = """
    private void resolveEffect(GameData gd, StackEntry entry, CardEffect effect) {
        EffectHandler handler = registry.getHandler(effect);
        handler.resolve(gd, entry, effect);
    }

"""

if "resolveEffect(GameData gd" not in src:
    src = src.replace(
        "        gd.activePlayerId = player1Id;\n    }",
        "        gd.activePlayerId = player1Id;" + setup + "\n    }",
    )
    src = src.replace(
        "    // ===== Helper methods =====",
        helper + "    // ===== Helper methods =====",
    )

TEST.write_text(src, encoding="utf-8")
remaining = len(re.findall(r"service\.", src))
print(f"Updated test. Remaining service. references: {remaining}")
