package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.GameData;

public interface TurnProgressionCallback {

    void advanceStep(GameData gameData);

    void resolveAutoPass(GameData gameData);
}
