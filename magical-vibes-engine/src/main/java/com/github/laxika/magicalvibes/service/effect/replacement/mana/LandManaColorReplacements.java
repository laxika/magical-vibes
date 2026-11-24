package com.github.laxika.magicalvibes.service.effect.replacement.mana;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ReplaceLandManaWithColorEffect;
import com.github.laxika.magicalvibes.model.replacement.LandManaEvent;
import com.github.laxika.magicalvibes.model.replacement.ReplacementOption;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService.LandManaColorReplacement;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Every {@link ReplaceLandManaWithColorEffect}, printed (Infernal Darkness, Contamination, Pulse of Llanowar) or
 * floated by a resolved spell or ability (Deep Water, Harvest Mage, Pale Moon, Chaos Moon, Hall of Gemstone,
 * Quarum Trench Gnomes): one option per replacement that applies, found by
 * {@link GameQueryService#landManaColorReplacements}. "Instead of any other type" makes the mana that type;
 * "instead of [types]" changes only mana of those types, and doesn't apply to a tap that makes none.
 */
@Component
class LandManaColorReplacements extends LandManaCandidateSource {

    private static final String KEY = "LandManaColor:";

    LandManaColorReplacements(GameQueryService gameQueryService) {
        super(gameQueryService);
    }

    @Override
    protected List<ReplacementOption> applicable(GameData gameData, LandManaEvent event, Permanent land) {
        List<ReplacementOption> options = new ArrayList<>();
        for (LandManaColorReplacement replacement : applying(gameData, event, land)) {
            options.add(option(KEY + replacement.key(), replacement.sourceName() + ": " + label(replacement.effect())));
        }
        return options;
    }

    @Override
    public LandManaEvent transform(GameData gameData, LandManaEvent event, ReplacementOption option) {
        Permanent land = gameQueryService.findPermanentById(gameData, event.permanentId());
        ReplaceLandManaWithColorEffect effect = applying(gameData, event, land).stream()
                .filter(replacement -> option.key().equals(KEY + replacement.key()))
                .map(LandManaColorReplacement::effect)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Mana replacement ended: " + option.key()));
        Set<ManaColor> produced = effect.color() == null ? anyColor() : Set.of(effect.color());
        LandManaEvent recolored = event.withColors(effect.replacedTypes().isEmpty() ? produced
                : replaceColors(event.colors(), effect.replacedTypes(), produced));
        return effect.fixedAmount() == null ? recolored : recolored.withAmount(effect.fixedAmount());
    }

    private List<LandManaColorReplacement> applying(GameData gameData, LandManaEvent event, Permanent land) {
        return gameQueryService.landManaColorReplacements(gameData, land, event.playerId()).stream()
                .filter(replacement -> replacement.effect().replacedTypes().isEmpty()
                        || event.colors().stream().anyMatch(replacement.effect().replacedTypes()::contains))
                .toList();
    }

    private static String label(ReplaceLandManaWithColorEffect effect) {
        String produced = effect.color() == null ? "a color of your choice" : manaText(effect.color());
        String amount = effect.fixedAmount() == null ? "" : " (amount " + effect.fixedAmount() + ")";
        if (effect.replacedTypes().isEmpty()) {
            return "produces " + produced + amount;
        }
        if (effect.replacedTypes().containsAll(ManaColor.COLORS)) {
            return "produces " + produced + " instead of any other color" + amount;
        }
        return "produces " + produced + " instead of " + String.join(" or ", effect.replacedTypes().stream()
                .sorted().map(LandManaCandidateSource::manaText).toList()) + amount;
    }
}
