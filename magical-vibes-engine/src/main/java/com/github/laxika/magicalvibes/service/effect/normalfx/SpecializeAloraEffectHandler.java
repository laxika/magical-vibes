package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.a.AloraRogueCompanion;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SpecializeAloraEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/** Applies Alora's five digital specialized faces to the permanent's runtime card. */
@Component
@RequiredArgsConstructor
public class SpecializeAloraEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SpecializeAloraEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SpecializeAloraEffect specialize = (SpecializeAloraEffect) effect;
        var source = entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null || !"Alora, Rogue Companion".equals(source.getCard().getName())) {
            return;
        }

        Card specialized = source.getCard().createRuntimeCopy();
        specialized.clearRulesTextAndAbilities();
        setBaseFaceCharacteristics(specialized);

        switch (specialize.color()) {
            case WHITE -> {
                specialized.setName("Alora, Cheerful Mastermind");
                specialized.setManaCost("{3}{W}{U}");
                specialized.setColors(List.of(CardColor.WHITE, CardColor.BLUE));
                specialized.setColorIdentity(List.of(CardColor.WHITE, CardColor.BLUE));
                specialized.setColor(CardColor.WHITE);
                specialized.setPower(4);
                specialized.setToughness(4);
            }
            case BLUE -> {
                specialized.setName("Alora, Cheerful Thief");
                specialized.setManaCost("{3}{U}{U}");
                specialized.setPower(4);
                specialized.setToughness(4);
            }
            case BLACK -> {
                specialized.setName("Alora, Cheerful Assassin");
                specialized.setManaCost("{3}{U}{B}");
                specialized.setColors(List.of(CardColor.BLUE, CardColor.BLACK));
                specialized.setColorIdentity(List.of(CardColor.BLUE, CardColor.BLACK));
                specialized.setSubtypes(List.of(CardSubtype.HALFLING, CardSubtype.ROGUE,
                        CardSubtype.ASSASSIN));
                specialized.setColor(CardColor.BLACK);
                specialized.setPower(4);
                specialized.setToughness(4);
            }
            case RED -> {
                specialized.setName("Alora, Cheerful Swashbuckler");
                specialized.setManaCost("{3}{U}{R}");
                specialized.setColors(List.of(CardColor.BLUE, CardColor.RED));
                specialized.setColorIdentity(List.of(CardColor.BLUE, CardColor.RED));
                specialized.setColor(CardColor.RED);
                specialized.setPower(4);
                specialized.setToughness(4);
            }
            case GREEN -> {
                specialized.setName("Alora, Cheerful Scout");
                specialized.setManaCost("{3}{G}{U}");
                specialized.setColors(List.of(CardColor.GREEN, CardColor.BLUE));
                specialized.setColorIdentity(List.of(CardColor.GREEN, CardColor.BLUE));
                specialized.setColor(CardColor.GREEN);
                specialized.setSubtypes(List.of(CardSubtype.HALFLING, CardSubtype.ROGUE,
                        CardSubtype.SCOUT));
                specialized.setPower(4);
                specialized.setToughness(4);
            }
            default -> throw new IllegalStateException("Unsupported Alora specialization color: "
                    + specialize.color());
        }

        specialized.setCardText(AloraRogueCompanion.specializedCardText(specialize.color()));
        AloraRogueCompanion.addAttackAbility(
                specialized, AloraRogueCompanion.specializedReturnEffect(specialize.color()));
        source.exchangeCard(specialized);
    }

    private void setBaseFaceCharacteristics(Card card) {
        card.setSetCode("HBG");
        card.setCollectorNumber("5");
        card.setType(CardType.CREATURE);
        card.setAdditionalTypes(Set.of());
        card.setSupertypes(EnumSet.of(CardSupertype.LEGENDARY));
        card.setSubtypes(List.of(CardSubtype.HALFLING, CardSubtype.ROGUE));
        card.setColor(CardColor.BLUE);
        card.setColors(List.of(CardColor.BLUE));
        card.setColorIdentity(List.of(CardColor.BLUE));
        card.setKeywords(Set.of());
    }
}
