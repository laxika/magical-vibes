package com.github.laxika.magicalvibes.service.effect.replacement.mana;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.replacement.LandManaEvent;
import com.github.laxika.magicalvibes.model.replacement.ReplacementOption;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.replacement.ReplacementCandidateSource;

import java.util.List;
import java.util.Set;

/**
 * Shared base for the land-mana replacement families. Every option it creates is a pure
 * {@link ReplacementOption.Kind#REPLACEMENT}: it only rewrites the mana about to be produced.
 */
abstract class LandManaCandidateSource implements ReplacementCandidateSource<LandManaEvent> {

    protected final GameQueryService gameQueryService;

    protected LandManaCandidateSource(GameQueryService gameQueryService) {
        this.gameQueryService = gameQueryService;
    }

    @Override
    public Class<LandManaEvent> eventType() {
        return LandManaEvent.class;
    }

    @Override
    public final List<ReplacementOption> applicable(GameData gameData, LandManaEvent event) {
        Permanent land = gameQueryService.findPermanentById(gameData, event.permanentId());
        if (land == null || event.amount() <= 0) {
            return List.of();
        }
        return applicable(gameData, event, land);
    }

    protected abstract List<ReplacementOption> applicable(GameData gameData, LandManaEvent event, Permanent land);

    protected static ReplacementOption option(String key, String label) {
        return new ReplacementOption(key, label, ReplacementOption.Kind.REPLACEMENT, true);
    }

    /** A produced mana type as a prompt label shows it, e.g. "{U}". */
    protected static String manaText(ManaColor color) {
        return "{" + color.getCode() + "}";
    }

    protected static Set<ManaColor> anyColor() {
        return Set.copyOf(ManaColor.COLORS);
    }
}
