package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.w.WyllPactBoundDuelist;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SpecializeWyllEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import org.springframework.stereotype.Component;

import java.util.List;

/** Applies Wyll's five digital specialized faces to the battlefield permanent. */
@Component
public class SpecializeWyllEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    public SpecializeWyllEffectHandler(GameQueryService gameQueryService) {
        this.gameQueryService = gameQueryService;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SpecializeWyllEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CardColor color = ((SpecializeWyllEffect) effect).color();
        Permanent source = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null || !"Wyll, Pact-Bound Duelist".equals(source.getCard().getName())) {
            return;
        }

        Card specialized = source.getCard().createRuntimeCopy();
        specialized.clearRulesTextAndAbilities();
        WyllPactBoundDuelist.setSpecializedBaseCharacteristics(specialized);
        specialized.setPower(5);
        specialized.setToughness(5);
        setFaceCharacteristics(specialized, color);

        TargetFilter targetFilter = WyllPactBoundDuelist.specializedTargetFilter(color);
        if (targetFilter != null) {
            specialized.target(targetFilter);
        }
        source.exchangeCard(specialized);

        StackEntry trigger = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                specialized,
                entry.getControllerId(),
                specialized.getName() + "'s ability",
                List.of(WyllPactBoundDuelist.specializedTrigger(color)),
                0,
                source.getId());
        trigger.setTargetFilter(targetFilter);
        trigger.setNonTargeting(targetFilter == null);
        gameData.enqueueTrigger(trigger);
    }

    private void setFaceCharacteristics(Card card, CardColor color) {
        switch (color) {
            case WHITE -> {
                card.setName("Wyll of the Celestial Pact");
                card.setManaCost("{3}{R}{R}{W}");
                card.setColors(List.of(CardColor.RED, CardColor.WHITE));
                card.setColorIdentity(List.of(CardColor.RED, CardColor.WHITE));
                card.setColor(CardColor.WHITE);
                card.setCardText("When this creature specializes, you may sacrifice another creature or an artifact. "
                        + "When you do, return target creature card from your graveyard to the battlefield. It gains "
                        + "haste. If its mana value is 4 or greater, sacrifice it at the beginning of your next end step.");
            }
            case BLUE -> {
                card.setName("Wyll of the Elder Pact");
                card.setManaCost("{3}{R}{R}{U}");
                card.setColors(List.of(CardColor.RED, CardColor.BLUE));
                card.setColorIdentity(List.of(CardColor.RED, CardColor.BLUE));
                card.setColor(CardColor.BLUE);
                card.setCardText("When this creature specializes, you may sacrifice another creature or an artifact. "
                        + "When you do, return target instant or sorcery card from your graveyard to your hand.");
            }
            case BLACK -> {
                card.setName("Wyll of the Fiend Pact");
                card.setManaCost("{3}{B}{R}{R}");
                card.setColors(List.of(CardColor.BLACK, CardColor.RED));
                card.setColorIdentity(List.of(CardColor.BLACK, CardColor.RED));
                card.setColor(CardColor.BLACK);
                card.setCardText("When this creature specializes, you may sacrifice another creature or an artifact. "
                        + "When you do, draw three cards unless target opponent pays 5 life.");
            }
            case RED -> {
                card.setName("Wyll of the Blade Pact");
                card.setManaCost("{3}{R}{R}{R}");
                card.setCardText("When this creature specializes, you may sacrifice another creature or an artifact. "
                        + "When you do, untap Wyll of the Blade Pact. After this main phase, there is an additional "
                        + "combat phase followed by an additional main phase.");
            }
            case GREEN -> {
                card.setName("Wyll of the Fey Pact");
                card.setManaCost("{3}{R}{R}{G}");
                card.setColors(List.of(CardColor.RED, CardColor.GREEN));
                card.setColorIdentity(List.of(CardColor.RED, CardColor.GREEN));
                card.setColor(CardColor.GREEN);
                card.setCardText("When this creature specializes, you may sacrifice another creature or an artifact. "
                        + "When you do, Wyll of the Fey Pact perpetually gets +3/+3 and gains trample.");
            }
        }
    }
}
