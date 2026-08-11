package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

public record PsychicBattleRetargetEffect(UUID spellCardId, int targetIndex) implements CardEffect {}
